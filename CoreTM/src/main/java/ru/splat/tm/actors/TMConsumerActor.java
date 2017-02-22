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
            trackers.put(topic, new TopicTracker(topic));
        }
        consumer.assign(partitions);
        Set<TopicPartition> partitionSet = consumer.assignment();
        resetToCommitedOffset(partitionSet);
        log.info("TMConsumerActor is initialized");
    }

    private void resetToCommitedOffset(Set<TopicPartition> partitionSet) { // Заставить работать

        for (TopicPartition partition : partitionSet) {
            try {
                consumer.seek(partition, consumer.committed(partition).offset());
                log.info("reset to commited offset for " + partition.topic());
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
            trackers.get(record.topic()).addRecord(record.key());
            ServiceResponseMsg srm = new ServiceResponseMsg(record.key(), ResponseParser.unpackMessage(record.value()),
                    TOPICS_MAP.get(record.topic()));
            //log.info("message received from : " + record.topic() + ": " + record.key() + " " + sr.getAttachment() );
            tmActor.tell(srm, getSelf());
            //getSelf().tell(new PollMsg(), getSelf());
        }
        //consumer.commitAsync();
        getContext().system().scheduler().scheduleOnce(Duration.create(250, TimeUnit.MILLISECONDS),
                getSelf(), new PollMsg(), getContext().dispatcher(), null);

    }

    private static Map<String, ServicesEnum> TOPICS_MAP;
    static {
        TOPICS_MAP = new HashMap<>();
        TOPICS_MAP.put("BetRes", ServicesEnum.BetService);
        TOPICS_MAP.put("EventRes", ServicesEnum.EventService);
        TOPICS_MAP.put("BillingRes", ServicesEnum.BillingService);
        TOPICS_MAP.put("PunterRes", ServicesEnum.PunterService);
    }


    private class TopicTracker {
        private ArrayList<Long> records = new ArrayList<>();  //проверить порядок оффсетов при poll() в список рекордов (разные топики), заменить на массив или ArrayList
        private final String topicName;
        private final long currentOffset;   //относительный оффсет (равен 0 при старте)
        private Set<Long> pendingTransactions= new HashSet<>();

        private TopicTracker(String topicName) {
            this.topicName = topicName;
            this.currentOffset = 0;
        }
        String getTopicName() {
            return topicName;
        }

        void addRecord(long trId) { //изменить логику
            if (records.contains(trId))
                records.add(-1l);       //trId -1 - индикатор лишнего сообщения (можно коммитить)
            else
                records.add(trId);
            log.info(topicName + ": record with id " + trId);
        }

        public long commit(long trId) {
            long offset = currentOffset;
            boolean commitable = false;
            for (long record : records) {

            }
            if (commitable)
                return offset;
            else
                return -1;
        }
        


    }
}

