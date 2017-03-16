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
import ru.splat.tm.util.TopicTracker;
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
    private final int MAX_POLL_INTERVAL = 100;
    private final long COMMIT_INTERVAL = 30000;
    private boolean kafkaConnected = false;
    private int seekCounter = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PollMsg.class, m -> poll())
                .match(CommitTransactionMsg.class, this::commitTransaction)
                .match(MarkSpareMsg.class, this::markSpareTransaction)
                .match(RetryCommitMsg.class, m -> {
                    log.info("retry commiting transaction topic");
                    processCommitTopic(m);
                })
                .match(CommitTopicMsg.class, this::processCommitTopic)
                .match(SeekSuccess.class, this::processSeekSucess)
                .matchAny(this::unhandled)
                .build();
    }

    private void processSeekSucess(SeekSuccess m) {
        seekCounter++;
        if (seekCounter == topics.length)
            kafkaConnected = true;
    }

    public TMConsumerActor() {
        this.tmActor = context().parent();
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commitTransaction", "false");
        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(),
                new ProtoBufMessageDeserializer(Response.ServiceResponse.getDefaultInstance()));
        //consumer.subscribe(Arrays.asList(topics));
        List<TopicPartition> partitions = new LinkedList<>();
        for (String topic : topics) {
            partitions.add(new TopicPartition(topic, 0));
        }
        consumer.assign(partitions); log.info("consumer assigned to partitions");
        //consumer.commitSync();
        //TODO почему-то по-другому не работает
        Set<TopicPartition> partitionSet = consumer.assignment();log.info("fetched assignment");partitionSet.forEach(partition -> log.info(partition.topic() + partition.partition()));
        resetToCommitedOffset(partitionSet);
        trackers.values().forEach(topicTracker -> {
            getContext().system().scheduler().scheduleOnce(Duration.create(COMMIT_INTERVAL, TimeUnit.MILLISECONDS),
                    getSelf(), new CommitTopicMsg(topicTracker.getTopicName()), getContext().dispatcher(), null);
        });
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
                trackers.put(partition.topic(), new TopicTracker(partition, offset, this.log));
                consumer.seek(partition, offset); log.info("seek");
                getSelf().tell(new SeekSuccess(), getSelf());

                log.info("created TopicTracker for topic " + partition.topic() + " with currentOffset on " + offset);
            }
        }
    }

    //make excess transaction message commitable
    private void markSpareTransaction(MarkSpareMsg m) {
        trackers.get(ResponseTopicMapper.getTopic(m.getService())).markTransaction(m.getOffset());
    }

    private void commitTransaction(CommitTransactionMsg m) {
        long time = System.currentTimeMillis();
        m.getServices().forEach(s -> {
            trackers.get(ResponseTopicMapper.getTopic(s)).commitTransaction(m.getTransactionId());;
        });
        log.info("commitTransaction " + m.getTransactionId() + "  took: " + (System.currentTimeMillis() - time));
        //log.info("Transaction " + m.getTransactionId() + " is commited");
    }
    //коммит топиков кафки по данным, имеющимся в топиктрекерах
    private void processCommitTopic(CommitTopicMsg m) {
        //trackers.get(m.getTopic()).commitTracker(m);
        TopicTracker tracker = trackers.get(m.getTopic());
        OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(tracker.getCurrentOffset());
        Map<TopicPartition,OffsetAndMetadata> commitMap = new HashMap<>(1);
        commitMap.put(tracker.getPartition(), offsetAndMetadata);
        consumer.commitAsync(commitMap, (metadata, e) -> {
            if (e == null) {
                log.info(tracker.getTopicName() + ": consumer commited to offset " + tracker.getCurrentOffset());
                getContext().system().scheduler().scheduleOnce(Duration.create(COMMIT_INTERVAL, TimeUnit.MILLISECONDS),
                        getSelf(), new CommitTopicMsg(tracker.getTopicName()), getContext().dispatcher(), null);
            }
            else {
                getSelf().tell(new RetryCommitMsg(m.getTopic()), getSelf());
            }
        });
    }
    public void start() {
        poll();
    }

    private void poll() {
        long time = System.currentTimeMillis();
        ConsumerRecords<Long, Response.ServiceResponse> records = consumer.poll(0);
        //log.info("messages consumed: " + records.count());
        for (ConsumerRecord<Long, Response.ServiceResponse> record : records) {
            //log.info("message received: " + record.key() + " from topic " + record.topic());
            if (!trackers.get(record.topic()).addRecord(record.offset(), record.key())) {
                ServiceResponseMsg srm = new ServiceResponseMsg(record.key(), ResponseParser.unpackMessage(record.value()),
                        ResponseTopicMapper.getService(record.topic()), record.offset());
                tmActor.tell(srm, getSelf());
            }
        }
              //коммит всех трекеров

        getContext().system().scheduler().scheduleOnce(Duration.create(MAX_POLL_INTERVAL - System.currentTimeMillis() + time, TimeUnit.MILLISECONDS),
                getSelf(), new PollMsg(), getContext().dispatcher(), null);
        //log.info("poll took: " + (System.currentTimeMillis() - time));
    }

    private static class SeekSuccess {


    }


}

