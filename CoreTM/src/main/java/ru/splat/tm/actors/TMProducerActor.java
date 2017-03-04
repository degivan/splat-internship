package ru.splat.tm.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.kafka.serializer.ProtoBufMessageSerializer;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.tm.messages.*;
import ru.splat.tm.protobuf.ProtobufFactory;
import ru.splat.tm.util.RequestTopicMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Дмитрий on 04.03.2017.
 */
public  class TMProducerActor extends AbstractActor {
    private KafkaProducer<Long, Message> producer;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final ActorRef tmActor;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMetadata.class, this::processTransaction)
                .matchAny(this::unhandled)
                .build();
    }
    //создание стейта транзакции при получении ответа от сервисов (заглушка для сложной процедуры коммита кафки)
    private void processTransaction(TransactionMetadata trMetadata) {
        long startTime = System.currentTimeMillis();
        List<LocalTask> taskList = trMetadata.getLocalTasks();
        long transactionId = trMetadata.getTransactionId();
        log.info("processing transaction " + transactionId + " with " + taskList.size() + " tasks");
        Set<ServicesEnum> services = taskList.stream().map(LocalTask::getService)
                .collect(Collectors.toSet());
        taskList.forEach(task->{
            Message message = ProtobufFactory.buildProtobuf(task, services);
            send(RequestTopicMapper.getTopic(task.getService()), transactionId, message);
        });
        log.info("processTransaction took " + (System.currentTimeMillis() - startTime));
    }
    private void send(String topic, Long transactionId, Message message) {
        //log.info("TMActor: sending " + transactionId + " to " + topic);
        /*Future isSend = */producer.send(new ProducerRecord<>(topic, transactionId, message),
                (metadata, e) -> {
                    if (e != null) tmActor.tell(new RetrySendMsg(topic, transactionId, message), getSelf());
                    else tmActor.tell(new TaskSentMsg(transactionId, RequestTopicMapper.getService(topic)), getSelf());
                });
    }
    public TMProducerActor() {
        Properties propsProducer = new Properties();
        this.tmActor = context().parent();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);
        producer = new KafkaProducer(propsProducer, new LongSerializer(), new ProtoBufMessageSerializer());
        log.info("TMProducerActor is initialized");

        /*getContext().system().scheduler().schedule(Duration.Zero(),
                Duration.create(500, TimeUnit.MILLISECONDS), consumerActor, new PollMsg(),
                getContext().system().dispatcher(), null);*/
    }
}
