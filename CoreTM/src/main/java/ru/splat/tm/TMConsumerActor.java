package ru.splat.tm;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.google.protobuf.Message;
import org.apache.kafka.common.serialization.LongDeserializer;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.messages.Response;
import ru.splat.tmactors.PollMessage;
import ru.splat.tmkafka.TMConsumer;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Дмитрий on 11.01.2017.
 */
public class TMConsumerActor extends AbstractActor{
    //KafkaConsumer<Long, Message> consumer;
    private final String[] topicsList =  {"BetRes", "BillingRes", "EventRes", "PunterRes"};
    private KafkaConsumer<Long, Response.ServiceResponse> consumer;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PollMessage.class, this::poll)
                .build();
    }

    public TMConsumerActor() {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "true");
        propsConsumer.put("auto.commit.interval.ms", "500");//пока что с автокоммитом
        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(),
                new ProtoBufMessageDeserializer(Response.ServiceResponse.getDefaultInstance()));
        consumer.subscribe(Arrays.asList(topicsList));
        System.out.println("consumer actor is here");


    }

    private void poll(PollMessage p) {
        ConsumerRecords<Long, Response.ServiceResponse> records = consumer.poll(100);
        System.out.println("TMConsumer: messages consumed");
        for (ConsumerRecord<Long, Response.ServiceResponse> record : records) {
            System.out.println("message received: " + record.key());
        }
    }




}
