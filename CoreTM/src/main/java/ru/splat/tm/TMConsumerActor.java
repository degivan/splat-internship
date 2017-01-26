package ru.splat.tm;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.google.protobuf.Message;
import ru.splat.tmkafka.TMConsumer;

import javax.annotation.PostConstruct;

/**
 * Created by Дмитрий on 11.01.2017.
 */
public class TMConsumerActor extends UntypedActor {
    //KafkaConsumer<Long, Message> consumer;
    private final ActorRef tmFinalizer;
    //кафковский консюмер вынесен в отдельный класс
    private final TMConsumer consumer;



    public TMConsumerActor(ActorRef tmFinalizer, TMConsumer consumer) {
        this.tmFinalizer = tmFinalizer;
        this.consumer = consumer;
    }
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message.equals("poll")) {
            System.out.println(message.toString());
            /*ConsumerRecords<Long, Message> records = consumer.pollRecords();
            records.forEach(record-> {
                tmFinalizer.tell((Object) record.value(), getSelf());
            }
            );*/
        }
        else throw new Exception("TMConsumerActor: Unknown message type");
    }
}

/*class Ticker extends UntypedActor {

    @Override
    public void onReceive(Object o) throws Throwable {

    }
}*/
