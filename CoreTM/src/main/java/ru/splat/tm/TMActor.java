package ru.splat.tm;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.kafka.serializer.ProtoBufMessageSerializer;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.messages.uptm.trstate.TransactionState;
import ru.splat.tmactors.ServiceResponseMsg;
import ru.splat.tmactors.TaskSent;
import ru.splat.tmprotobuf.ProtobufFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by Дмитрий on 05.01.2017.
 */
public  class TMActor extends AbstractActor {
    @Autowired
    private ProtobufFactory protobufFactory;
    private KafkaProducer<Long, Message> producer;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMetadata.class, m -> {
                    createTransactionState(m);
                    processTransaction(m);
                })
                .match(TaskSent.class, this::setIsSent)
                .match(ServiceResponseMsg.class, this::processResponse)
                .build();
    }

    public void setIsSent(TaskSent m) {
        System.out.println("task " + m.getTaskType().toString() + " of " + m.getTransactionId() + " is sent");
    }

    //создание стейта транзакции при получении ответа от сервисов (а надо ли?)
    public void createTransactionState(Long transactionId, Map<TaskTypesEnum, ServiceResponse> localStates) {

    }
    //создание стейта транзакции из метадаты
    public void createTransactionState(TransactionMetadata transactionMetadata) {
        /*Map<TaskTypesEnum, ServiceResponse> responseMap = transactionMetadata.getLocalTasks().stream()
                .map(localTask -> new ServiceResponse())

        TransactionState transactionState = new TransactionState(transactionMetadata.getTransactionId(),
                );*/
    }



    public void processTransaction(TransactionMetadata trMetadata) {
        List<LocalTask> taskList = trMetadata.getLocalTasks();
        Long transactionId = trMetadata.getTransactionId();
        System.out.println("TMStarter: transactionId " + transactionId);
        //отправка кафке
        //быдлокод
        Set<ServicesEnum> services = taskList.stream().map(task -> task.getService())
                .collect(Collectors.toSet());
        List<String> taskNames = new ArrayList<>();
        taskList.forEach(task->{
            Message message = null;
            message = protobufFactory.buildProtobuf(task, services);
            //requests.add(new RequestContainer(transactionId, TOPICS_MAP.get(task.getService()), message));  //добавили в пачку
            //send(TOPICS_MAP.get(task.getService()), transactionId, message);
        });
    }

    private void send(String topic, Long transactionId, Message message) {
        System.out.println("TMStarter: topic " + topic);
        ProducerRecord<Long, Message> pr = new ProducerRecord<Long, Message>(topic, transactionId, message);
        /*Future isSend = producer.send(new ProducerRecord<Long, Message>(topic, transactionId, message),
                (metadata, e) -> if(e) getSelf().tell());*/
    }

    private void processResponse(ServiceResponseMsg sr) {

    }

    public TMActor() {
        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);
        producer = new KafkaProducer(propsProducer, new LongSerializer(), new ProtoBufMessageSerializer());
    }
    private static Map<ServicesEnum, String> TOPICS_MAP;

    static {
        TOPICS_MAP = new HashMap<>();
        TOPICS_MAP.put(ServicesEnum.BetService, "BetReq");
        TOPICS_MAP.put(ServicesEnum.EventService, "EventReq");
        TOPICS_MAP.put(ServicesEnum.BillingService, "BillingReq");
        TOPICS_MAP.put(ServicesEnum.PunterService, "PunterReq");
    }



}


