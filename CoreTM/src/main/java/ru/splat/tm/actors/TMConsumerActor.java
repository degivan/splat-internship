package ru.splat.tm.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.messages.Response;
import ru.splat.tm.messages.*;
import ru.splat.tm.protobuf.ResponseParser;
import ru.splat.tm.util.ResponseTopicMapper;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by Дмитрий on 11.01.2017.
 */
public class TMConsumerActor extends AbstractActor{
    //KafkaConsumer<Long, Message> consumer;
    private final String[] topics =  {"BetRes", "BillingRes", "EventRes", "PunterRes"};
    private final Map<String, TopicTracker> trackers = new HashMap<>();
    private KafkaConsumer<Long, Response.ServiceResponse> consumer;
    private final ActorRef tmActor;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PollMsg.class, m -> poll())
                .match(CommitTransactionMsg.class, this::commitTransaction)
                .match(MarkSpareMsg.class, this::markSpareTransaction)
                .match(RetryCommitMsg.class, m -> {
                    log.info("retry commiting transaction " + m.getTransactionId());
                    commitTransaction(m);
                })
                .matchAny(this::unhandled)
                .build();
    }

    public TMConsumerActor() {
        this.tmActor = context().parent();
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "false");
        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(),
                new ProtoBufMessageDeserializer(Response.ServiceResponse.getDefaultInstance()));
        //consumer.subscribe(Arrays.asList(topics));
        List<TopicPartition> partitions = new LinkedList<>();
        for (String topic : topics) {
            partitions.add(new TopicPartition(topic, 0));
        }
        consumer.assign(partitions); log.info("assigned");
        //consumer.commitSync();

        Set<TopicPartition> partitionSet = consumer.assignment();log.info("fetched assignment");partitionSet.forEach(partition -> log.info(partition.topic() + partition.partition()));
        resetToCommitedOffset(partitionSet);
        log.info("TMConsumerActor is initialized");
    }


    private void resetToCommitedOffset(Set<TopicPartition> partitionSet) {
        for (TopicPartition partition : partitionSet) {
            long offset = 0;
            try {
                offset = consumer.committed(partition).offset();    log.info(partition.topic() + partition.partition() + " offset is found " + offset);


                //log.info("reset to commited offset for " + partition.topic());
            }
            catch (NullPointerException e) {
                log.info(partition.topic() + " offset is null");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                trackers.put(partition.topic(), new TopicTracker(partition, offset));
                consumer.seek(partition, offset); log.info("seek");
                log.info("created TopicTracker for topic " + partition.topic() + " with currentOffset on " + offset);
            }
        }
    }

    //make excess transaction message commitable
    private void markSpareTransaction(MarkSpareMsg m) {
        trackers.get(ResponseTopicMapper.getTopic(m.getService())).markTransaction(m.getOffset());
    }

    private void commitTransaction(CommitTransactionMsg m) {

        trackers.values().forEach(tracker -> {
            long offset = tracker.commit(m.getTransactionId());
            if (offset != -1) {
                OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset);
                Map<TopicPartition,OffsetAndMetadata> commitMap = new HashMap<>(1);
                commitMap.put(tracker.getPartition(), offsetAndMetadata);
                consumer.commitAsync(commitMap, (metadata, e) -> {
                    if (e == null) log.info("Transaction " + m.getTransactionId() + " is commited in topic " + tracker.getTopicName());
                    else {
                        getSelf().tell(new RetryCommitMsg(m.getTransactionId()), getSelf());
                    }
                });
            }
            else
                log.info("Transaction " + m.getTransactionId() + " cannot be commited in topic " + tracker.getTopicName());
        });
        //log.info("Transaction " + m.getTransactionId() + " is commited");
    }

    public void start() {
        poll();
    }

    private void poll() {
        ConsumerRecords<Long, Response.ServiceResponse> records = consumer.poll(0);
        for (ConsumerRecord<Long, Response.ServiceResponse> record : records) {
            //log.info("message received: " + record.key() + " from topic " + record.topic());
            trackers.get(record.topic()).addRecord(record.offset(), record.key());
            ServiceResponseMsg srm = new ServiceResponseMsg(record.key(), ResponseParser.unpackMessage(record.value()),
                    ResponseTopicMapper.getService(record.topic()), record.offset());
            //log.info("message received from : " + record.topic() + ": " + record.key() + " " + sr.getAttachment() );
            tmActor.tell(srm, getSelf());
        }
        getContext().system().scheduler().scheduleOnce(Duration.create(250, TimeUnit.MILLISECONDS),
                getSelf(), new PollMsg(), getContext().dispatcher(), null);
        //log.info("poll ");

    }

    //TODO: проверить, соблюдается ли относительный порядок рекордов в ArrayList
    private class TopicTracker {
        private Map<Long, Long> records = new HashMap<>();  //TODO:проверить порядок оффсетов при poll() в список рекордов (разные топики), заменить на массив или ArrayList
        private final String topicName;
        private final TopicPartition partition;
        private long currentOffset;   //текущий оффсет консюмера
        private Set<Long> commitedTransactions= new HashSet<>();

        private TopicTracker(TopicPartition partition, long currentOffset) {
            this.topicName = partition.topic();
            this.partition = partition;
            this.currentOffset = currentOffset;
        }
        String getTopicName() {
            return topicName;
        }

        void addRecord(long offset, long trId) { //TODO:изменить логику добавления повторного сообщения(если потребуется)
            if (records.containsValue(trId))
                records.put(offset, -1l);       //trId -1 - индикатор лишнего сообщения (можно коммитить)
            else
                records.put(offset, trId);
            //log.info(topicName + ": record with id " + trId);
        }
        //возвращает оффсет (абсолютный) до которого можно коммитить или -1, если коммитить пока нельзя
        long commit(long trId) {
            commitedTransactions.add(trId); //добавляем эту транзакцию в закоммиченные
            log.info(topicName + ": currentOffset:  " + currentOffset); //StringBuilder sb = new StringBuilder();
            //records.entrySet().forEach(entry -> sb.append(entry.getKey() + " : " + entry.getValue() + " | ")); log.info(sb.toString());
            long offset = currentOffset;
            boolean commitable = false;
            while(true) {   //TODO исправить сей быдлоцикл
                Long record = records.get(offset);
                if (record == null || !(commitedTransactions.contains(record) || record == -1)) break;
                else {
                    offset++;
                    if (record == trId)
                        commitable = true;
                }
            }
            if (commitable) {
                log.info("commiting " + (offset - currentOffset) + " records");
                currentOffset = offset;
                return offset;
            }
            else
                return -1;
        }
        //make excess transaction message commitable
        void markTransaction(long offset) {
            //log.info("excess message is caught!!! offset: " + offset + " topic: " + topicName); //for testing
            records.put(offset, -1l);
        }

        public TopicPartition getPartition() {
            return partition;
        }
    }
}

