package ru.splat.tmkafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.messages.Response;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.tmactors.ServiceResponseMsg;
import ru.splat.tmprotobuf.ResponseParser;
import ru.splat.tmprotobuf.ResponseParserImpl;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Дмитрий on 06.01.2017.
 */
//simple auto-commit consumer for TMFinalizer
public class TMConsumerImpl implements TMConsumer {
    //уточнить у Ильнара имена топиков
    private final String[] topicsList =  {"BetRes", "BillingRes", "EventRes", "PunterRes"};
    private KafkaConsumer<Long, Response.ServiceResponse> consumer;
    @Autowired
    private ResponseParser responseParser;

    public TMConsumerImpl() {
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "true");
        propsConsumer.put("auto.commit.interval.ms", "500");//пока что с автокоммитом
        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(),
                new ProtoBufMessageDeserializer(Response.ServiceResponse.getDefaultInstance()));
        consumer.subscribe(Arrays.asList(topicsList));
        responseParser = new ResponseParserImpl();
        System.out.println("consumer here");


    }

    public ConsumerRecords<Long, Response.ServiceResponse> pollRecords() {
        ConsumerRecords<Long, Response.ServiceResponse> records = consumer.poll(100);
        System.out.println("TMConsumer: messages consumed");
        for (ConsumerRecord<Long, Response.ServiceResponse> record : records) {
            System.out.println("message received: " + record.key());
            ServiceResponse sr = responseParser.unpackMessage(record.value());
            ServiceResponseMsg srm = new ServiceResponseMsg(record.key(), sr);
        }
        return records;
    }
}
