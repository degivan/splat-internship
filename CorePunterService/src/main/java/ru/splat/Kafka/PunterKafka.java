package ru.splat.Kafka;


import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import ru.splat.protobuf.PunterReq;
import ru.splat.protobuf.PunterRes;



public class PunterKafka implements Kafka<PunterReq.Punter, PunterRes.Punter> {

    private static KafkaProducer<Long, PunterRes.Punter> producer;
    private static KafkaConsumer<Long, PunterReq.Punter> consumer;
    public static final String TOPIC_REQUEST = "mytopic3";

    public void init(){
        init(PunterReq.Punter.getDefaultInstance(),TOPIC_REQUEST);
    }

    @Override
    public KafkaProducer<Long, PunterRes.Punter> getProducer() {
        return producer;
    }

    @Override
    public void setConsumer(KafkaConsumer<Long, PunterReq.Punter> consumer2) {
        consumer = consumer2;
    }

    @Override
    public void setProducer(KafkaProducer<Long, PunterRes.Punter> producer2) {
        producer = producer2;
    }

    @Override
    public String getTopicRequest() {
        return TOPIC_REQUEST;
    }

    @Override
    public String getTopicResponse() {
        return null;
    }

    @Override
    public KafkaConsumer<Long, PunterReq.Punter> getConsumer() {
        return consumer;
    }
}
