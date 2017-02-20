package ru.splat.tm.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.tm.messages.CommitTransactionMsg;
import ru.splat.tm.messages.PollMsg;
import ru.splat.tm.messages.ServiceResponseMsg;
import ru.splat.tm.protobuf.ResponseParser;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * Created by Дмитрий on 11.01.2017.
 */
public class TMConsumerActor extends AbstractActor{
    //KafkaConsumer<Long, Message> consumer;
    private final String[] topicsList =  {"BetRes", "BillingRes", "EventRes", "PunterRes"};
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
        propsConsumer.put("enable.auto.commit", "true");
        propsConsumer.put("auto.commit.interval.ms", "500");//пока что с автокоммитом
        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(),
                new ProtoBufMessageDeserializer(Response.ServiceResponse.getDefaultInstance()));
        consumer.subscribe(Arrays.asList(topicsList));
        resetToCommitedOffset();
        log.info("TMConsumerActor: initialized");
        start();
    }

    private void resetToCommitedOffset() {
        /*for (String topic : topicsList) {
            TopicPartition partition = new TopicPartition(topic, 0);
            consumer.seek(partition, consumer.committed(partition).offset());
        }*/
    }

    private void commitTransaction(CommitTransactionMsg m) {
        log.info("Transaction " + m.getTransactionId() + " is commited");
    }

    public void start() {
        poll();
    }

    private void poll() {
        ConsumerRecords<Long, Response.ServiceResponse> records = consumer.poll(0);
        for (ConsumerRecord<Long, Response.ServiceResponse> record : records) {
            //log.info("message received: " + record.key());
            ServiceResponseMsg srm = new ServiceResponseMsg(record.key(), ResponseParser.unpackMessage(record.value()),
                    TOPICS_MAP.get(record.topic()));
            //log.info("TMConsumerActor: message received from : " + record.topic() + ": " + record.key() + " " + sr.getAttachment() );
            tmActor.tell(srm, getSelf());
            //getSelf().tell(new PollMsg(), getSelf());
        }
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


    private class topicTracker {
        private Map<Long, Long> topicsMap = new HashMap<>();
        private final String topicName;

        private topicTracker(String topicName) {
            this.topicName = topicName;
        }
    }
}

