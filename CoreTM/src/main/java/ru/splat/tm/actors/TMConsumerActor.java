package ru.splat.tm.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.tm.messages.CommitTransactionMsg;
import ru.splat.tm.messages.MarkSpareMsg;
import ru.splat.tm.messages.PollMsg;
import ru.splat.tm.messages.ServiceResponseMsg;
import ru.splat.tm.protobuf.ResponseParser;
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
                .matchAny(this::unhandled)
                .build();
    }

    public TMConsumerActor(ActorRef tmActor) {
        this.tmActor = tmActor;
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
        consumer.assign(partitions);
        Set<TopicPartition> partitionSet = consumer.assignment();
        resetToCommitedOffset(partitionSet);
        log.info("TMConsumerActor is initialized");
    }
    //make excess transaction message commitable
    private void markSpareTransaction(MarkSpareMsg m) {
        trackers.get(SERVICE_TO_TOPIC_MAP.get(m.getService())).markTransaction(m.getOffset());
    }

    private void resetToCommitedOffset(Set<TopicPartition> partitionSet) {
        for (TopicPartition partition : partitionSet) {
            try {
                consumer.seek(partition, consumer.committed(partition).offset());
                trackers.put(partition.topic(), new TopicTracker(partition.topic(),consumer.committed(partition).offset()));
                log.info("created TopicTracker for topic " + partition.topic() + " with currentOffset on " + consumer.committed(partition).offset());
                //log.info("reset to commited offset for " + partition.topic());
            }
            /*catch (NullPointerException e) {
                log.info(partition.topic() + " offset is null");
            }*/
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void commitTransaction(CommitTransactionMsg m) {
        trackers.values().forEach(tracker -> {
            long relOffset = tracker.commit(m.getTransactionId());
            if (relOffset != -1) {

            }
        });

        log.info("Transaction " + m.getTransactionId() + " is commited");
    }

    public void start() {
        poll();
    }

    private void poll() {
        //log.info("poll");
        ConsumerRecords<Long, Response.ServiceResponse> records = consumer.poll(0);
        for (ConsumerRecord<Long, Response.ServiceResponse> record : records) {
            //log.info("message received: " + record.key());
            trackers.get(record.topic()).addRecord(record.offset(), record.key());
            ServiceResponseMsg srm = new ServiceResponseMsg(record.key(), ResponseParser.unpackMessage(record.value()),
                    TOPIC_TO_SERVICE_MAP.get(record.topic()), record.offset());
            //log.info("message received from : " + record.topic() + ": " + record.key() + " " + sr.getAttachment() );
            tmActor.tell(srm, getSelf());

            //getSelf().tell(new PollMsg(), getSelf());
        }
        //consumer.commitAsync();
        getContext().system().scheduler().scheduleOnce(Duration.create(250, TimeUnit.MILLISECONDS),
                getSelf(), new PollMsg(), getContext().dispatcher(), null);

    }


    private static Map<ServicesEnum, String> SERVICE_TO_TOPIC_MAP;  //TODO: поменять на BiMap? перенести в отдельный класс helper
    private static Map<String, ServicesEnum> TOPIC_TO_SERVICE_MAP;
    static {
        SERVICE_TO_TOPIC_MAP = new HashMap<>();
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.BetService, "BetRes");
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.EventService, "EventRes");
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.BillingService, "BillingRes");
        SERVICE_TO_TOPIC_MAP.put(ServicesEnum.PunterService, "unterRes");
        TOPIC_TO_SERVICE_MAP = new HashMap<>();
        TOPIC_TO_SERVICE_MAP.put("BetRes", ServicesEnum.BetService);
        TOPIC_TO_SERVICE_MAP.put("EventRes", ServicesEnum.EventService);
        TOPIC_TO_SERVICE_MAP.put("BillingRes", ServicesEnum.BillingService);
        TOPIC_TO_SERVICE_MAP.put("PunterRes", ServicesEnum.PunterService);
    }


    //TODO: проверить, соблюдается ли относительный порядок рекордов в ArrayList
    private class TopicTracker {
        private Map<Long, Long> records = new HashMap<>();  //TODO:проверить порядок оффсетов при poll() в список рекордов (разные топики), заменить на массив или ArrayList
        private final String topicName;
        private final long currentOffset;   //текущий оффсет консюмера
        private Set<Long> commitedTransactions= new HashSet<>();

        private TopicTracker(String topicName, long currentOffset) {
            this.topicName = topicName;
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
            log.info(topicName + ": record with id " + trId);
        }
        //возвращает число сообщений которые можно закоммитить или -1, если коммитить пока нельзя
        long commit(long trId) {
            commitedTransactions.add(trId); //добавляем эту транзакцию в закоммиченные
            long offset = currentOffset;
            boolean commitable = true;
            /*for ()
            for (long record : records) {
                if (!(commitedTransactions.contains(record) || record != -1)) {
                    commitable = false;
                    break;
                }
                else {
                    offset++;
                }
            }*/
            if (commitable)
                return offset;
            else
                return -1;
        }
        //make excess transaction message commitable
        void markTransaction(long offset) {
            records.put(offset, -1l);
        }




    }
}

