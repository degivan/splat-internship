package ru.splat.tm.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.tm.messages.PollMsg;
import ru.splat.tm.messages.ServiceResponseMsg;
import ru.splat.tm.protobuf.ResponseParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Дмитрий on 11.01.2017.
 */
public class TMConsumerActor extends AbstractActor{
    //KafkaConsumer<Long, Message> consumer;
    private final String[] topicsList =  {"BetRes", "BillingRes", "EventRes", "PunterRes"};
    private KafkaConsumer<Long, Response.ServiceResponse> consumer;
    private final ActorRef tmActor;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PollMsg.class, this::poll)
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
        //responseParser = new ResponseParserImpl();
        System.out.println("consumer actor is here");


    }



    private void poll(PollMsg p) {
        ConsumerRecords<Long, Response.ServiceResponse> records = consumer.poll(100);
        System.out.println("TMConsumer: messages consumed");
        for (ConsumerRecord<Long, Response.ServiceResponse> record : records) {
            //System.out.println("message received: " + record.key());
            ServiceResponse sr = ResponseParser.unpackMessage(record.value());
            ServiceResponseMsg srm = new ServiceResponseMsg(record.key(), sr, TOPICS_MAP.get(record.topic()));
            System.out.println("message received: " + record.key() + " " + sr.getAttachment());
            tmActor.tell(srm, getSelf());
        }
    }


    private static Map<String, ServicesEnum> TOPICS_MAP;

    static {
        TOPICS_MAP = new HashMap<>();
        TOPICS_MAP.put("BetRes", ServicesEnum.BetService);
        TOPICS_MAP.put("EventRes", ServicesEnum.EventService);
        TOPICS_MAP.put("BillingRes", ServicesEnum.BillingService);
        TOPICS_MAP.put("PunterRes", ServicesEnum.PunterService);
    }




}
