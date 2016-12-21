package ru.splat;

import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.feautures.BetInfo;
import ru.splat.feautures.TransactionResult;
import ru.splat.protobuf.ProtoBufMessageDeserializer;
import ru.splat.protobuf.ProtoBufMessageSerializer;
import ru.splat.protobuf.PunterReq;
import ru.splat.repository.PunterRepository;

import java.util.*;


public abstract class ServiceFacade<T,V> {

    protected static final String FIRST_PHASE = "phase1";
    protected static final String SECOND_PHASE = "phase2";
    protected static final String CANCEL_PHASE = "cancel";

    protected abstract Map<String, Set<TransactionResult>> processMessage(ConsumerRecords<Long, PunterReq.Person> consumerRecords);

    @Autowired
    protected PunterRepository punterRepository;

    protected KafkaProducer<Long, V> producer;
    protected KafkaConsumer<Long, T> consumer;

    public void init(Message consumerMessage, String topic) {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "false");
     //   propsConsumer.put("session.timeout.ms", "30000");

        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(), new ProtoBufMessageDeserializer(consumerMessage));
        consumer.subscribe(Arrays.asList(topic));

        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);

        producer = new KafkaProducer(propsProducer, new LongSerializer(), new ProtoBufMessageSerializer());
    }

    protected abstract Map<String, Set<TransactionResult>> runTasks(Map<String, Set<BetInfo>> filter);

    protected abstract Map filterSeen(ConsumerRecords<Long, T> consumerRecords);

    // public abstract Map<String,Set<TransactionResult>> process();
}
