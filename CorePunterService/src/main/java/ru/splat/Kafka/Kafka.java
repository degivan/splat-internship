package ru.splat.Kafka;

import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.protobuf.ProtoBufMessageDeserializer;
import ru.splat.protobuf.ProtoBufMessageSerializer;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Rustam on 1/3/2017.
 */
public interface Kafka<T,V> {

    default void init(Message consumerMessage, String topic) {

        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "false");
        // propsConsumer.put("session.timeout.ms", "30000");

        KafkaConsumer<Long, T> consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(), new ProtoBufMessageDeserializer(consumerMessage));
        consumer.subscribe(Arrays.asList(topic));
        setConsumer(consumer);

        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);

        KafkaProducer<Long, V> producer = new KafkaProducer(propsProducer, new LongSerializer(), new ProtoBufMessageSerializer());
        setProducer(producer);
    }

    KafkaConsumer<Long,T> getConsumer();
    KafkaProducer<Long,V> getProducer();
    void setConsumer(KafkaConsumer<Long,T> consumer);
    void setProducer(KafkaProducer<Long,V> producer);
    String getTopicRequest();
    String getTopicResponse();

}
