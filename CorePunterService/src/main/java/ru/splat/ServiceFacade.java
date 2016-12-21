package ru.splat;

import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.feautures.BetInfo;
import ru.splat.feautures.TransactionResult;
import ru.splat.protobuf.ProtoBufMessageDeserializer;
import ru.splat.protobuf.ProtoBufMessageSerializer;
import ru.splat.protobuf.PunterReq;
import ru.splat.repository.PunterRepository;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public abstract class ServiceFacade<T,V> {

    protected static final String FIRST_PHASE = "phase1";
    protected static final String SECOND_PHASE = "phase2";
    protected static final String CANCEL_PHASE = "cancel";

    protected abstract Map<String, Set<TransactionResult>> processMessage(ConsumerRecords<String, PunterReq.Person> consumerRecords);

    @Autowired
    protected PunterRepository punterRepository;

    protected KafkaProducer<String, V> producer;
    protected KafkaConsumer<String, T> consumer;

    public void init(Message consumerMessage, String topic) {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "false");
     //   propsConsumer.put("session.timeout.ms", "30000");

        consumer = new KafkaConsumer(propsConsumer, new StringDeserializer(), new ProtoBufMessageDeserializer(consumerMessage));
        consumer.subscribe(Arrays.asList(topic));

        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);

        producer = new KafkaProducer(propsProducer, new StringSerializer(), new ProtoBufMessageSerializer());
    }

    protected abstract Map<String, Set<TransactionResult>> runTasks(Map<String, Set<BetInfo>> filter);

    protected abstract Map filterSeen(ConsumerRecords<String, T> consumerRecords);

    // public abstract Map<String,Set<TransactionResult>> process();
}
