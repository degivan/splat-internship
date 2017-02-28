package ru.splat.tm.mocks;

import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import ru.splat.tm.messages.ServiceResponseMsg;
import ru.splat.tm.protobuf.ResponseParser;
import ru.splat.tm.util.ResponseTopicMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by Дмитрий on 05.02.2017.
 */
public class ServiceMock implements Runnable{
    private KafkaProducer<Long, Message> producer;
    private KafkaConsumer<Long, Message> consumer;
    private final org.slf4j.Logger LOGGER = getLogger(TMActor.class);
    private final String[] topics =  {"BetReq", "BillingReq", "EventReq", "PunterReq"};
    private long counter;
    

    public ServiceMock() {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "perftest3");
        propsConsumer.put("enable.auto.commit", "true");

        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(),
                new ProtoBufMessageDeserializer(BetRequest.Bet.getDefaultInstance()));
        consumer.subscribe(Arrays.asList(topics));

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
        sendMockResponse("BetRes", 111L, message1);
        sendMockResponse("BetRes", 111L, message1);
        sendMockResponse("BetRes", 111L, message1);
        sendMockResponse("BetRes", 111L, message1);
        Message message2 = Response.ServiceResponse.newBuilder()    //к BetService это не относится, ну и ладно
                .setBooleanAttachment(true).setResult(1).build();
        sendMockResponse("PunterRes", 111L, message2);
        sendMockResponse("PunterRes", 111L, message2);
        try {
            Thread.sleep(2000);
            sendMockResponse("PunterRes", 111L, message2);  //запоздалое сообщение, должно быть проигнорировано (помечено как commitable по-умолчанию)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void sendMockResponse(String topic, long transactionId, Message message) {
        producer.send(new ProducerRecord<>(topic, transactionId, message),
                (metadata, e) -> {
                    if (e != null) sendMockResponse(topic, transactionId, message);
                    else System.out.println(transactionId + " sent to " + topic);
                });
    }

    private void pollRoutine() {
        Set<Integer> servicesOrd = new HashSet<>(); servicesOrd.add(0); servicesOrd.add(1); servicesOrd.add(2); servicesOrd.add(3);
        while (true) {
            long time = System.currentTimeMillis();
            ConsumerRecords<Long, Message> records = consumer.poll(0);
            counter += records.count();
            System.out.println("messages consumed this poll: " + records.count());
            System.out.println("messages consumed: " + counter);
            for (ConsumerRecord<Long, Message>  record : records) {
                Response.ServiceResponse message = Response.ServiceResponse.newBuilder().addAllServices(servicesOrd)
                        .setBooleanAttachment(true).setResult(1).build();
                switch (record.topic()) {
                    case "BetReq":
                        sendMockResponse("BetRes", record.key(), message);
                        break;
                    case "BillingReq":
                        sendMockResponse("BillingRes", record.key(), message);
                        break;
                    case "EventReq":
                        sendMockResponse("EventRes", record.key(), message);
                        break;
                    case "PunterReq":
                        sendMockResponse("PunterRes", record.key(), message);
                        break;
                }
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }





    }

    @Override
    public void run() {
        pollRoutine();
    }
    // propsConsumer.put("session.timeout.ms", "30000");



}
