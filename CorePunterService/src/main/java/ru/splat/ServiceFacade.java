package ru.splat;

import com.google.protobuf.Message;
import com.sun.org.apache.bcel.internal.generic.ATHROW;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.feautures.BetInfo;

import ru.splat.protobuf.ProtoBufMessageDeserializer;
import ru.splat.protobuf.ProtoBufMessageSerializer;
import ru.splat.protobuf.PunterReq;
import ru.splat.protobuf.PunterRes;
import ru.splat.repository.IdempRepository;
import ru.splat.repository.PunterRepository;

import java.util.*;


public abstract class ServiceFacade<T,V,X> {

    protected static final String FIRST_PHASE = "phase1";
    protected static final String SECOND_PHASE = "phase2";
    protected static final String CANCEL_PHASE = "cancel";
    protected static String topic;

    @Autowired
    protected PunterRepository punterRepository;
    @Autowired
    protected IdempRepository idempRepository;

    protected KafkaProducer<Long, V> producer;
    protected KafkaConsumer<Long, T> consumer;


    public void init(Message consumerMessage) {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "false");
     // propsConsumer.put("session.timeout.ms", "30000");

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

    protected void mainProcess() throws InterruptedException {
        while (true) {
            try {
                ConsumerRecords<Long, T> consumerRecords = consumer.poll(1);
                Map<String, Set<TransactionResult>> map = processMessage(consumerRecords);
               // sendResult(map);
                consumer.commitSync();
            } catch (Exception e) {
                e.printStackTrace();
                TopicPartition partition = new TopicPartition(topic, 0);
                consumer.seek(partition, consumer.committed(partition).offset());
            }
        }
    }

    protected abstract void sendResult(Map<String, Set<TransactionResult>> map);

    protected abstract Map<String,Set<TransactionResult>> processMessage(ConsumerRecords<Long, T> consumerRecords) throws Exception;

    protected abstract Map<String, Set<TransactionResult>> runTasks(Map<String, Set<X>> filter);

    protected abstract Map filterSeen(ConsumerRecords<Long, T> consumerRecords);

    // public abstract Map<String,Set<TransactionResult>> process();
}
