package ru.splat.tm.mocks;

import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.kafka.serializer.ProtoBufMessageSerializer;
import ru.splat.messages.BetRequest;
import ru.splat.messages.Response;
import ru.splat.tm.LoggerGlobal;
import ru.splat.tm.actors.TMActor;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by Дмитрий on 05.02.2017.
 */
public class ServiceMock {
    KafkaProducer<Long, Message> producer;
    KafkaConsumer<Long, Message> consumer;
    private final org.slf4j.Logger LOGGER = getLogger(TMActor.class);
    

    public ServiceMock() {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "false");
        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(),
                new ProtoBufMessageDeserializer(BetRequest.Bet.getDefaultInstance()));

        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);
        producer = new KafkaProducer(propsProducer, new LongSerializer(), new ProtoBufMessageSerializer());
    }

    public void sendRoutine() {
        Message message1 = Response.ServiceResponse.newBuilder()
                .setLongAttachment(100L).setResult(1).build();
        sendMockResponse("BetRes", 111L, message1);
        Message message2 = Response.ServiceResponse.newBuilder()    //к BetService это не относится, ну и ладно
                .setBooleanAttachment(true).setResult(1).build();
        sendMockResponse("PunterRes", 111L, message2);

    }


    private void sendMockResponse(String topic, Long transactionId, Message message) {
        Future future = producer.send(new ProducerRecord<Long, Message>(topic, transactionId, message));
        try {
            future.get();
        } catch (InterruptedException e) {
            LoggerGlobal.log("InterruptedException");
        } catch (ExecutionException e) {
            LoggerGlobal.log("ExecutionException");
        }
        finally {
            LoggerGlobal.log("sent to " + topic);
        }
    }


    // propsConsumer.put("session.timeout.ms", "30000");



}
