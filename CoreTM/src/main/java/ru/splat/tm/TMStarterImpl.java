package ru.splat.tm;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.kafka.serializer.ProtoBufMessageSerializer;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.tmprotobuf.ProtobufFactory;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.tmprotobuf.ProtobufFactoryImpl;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by Дмитрий on 11.12.2016.
 */
//класс, отправляющий таски во входящие топики сервисов
    //возможно, стоит добавить отправку сообщений пачкой
public class TMStarterImpl implements TMStarter {
    private KafkaProducer<Long, Message> producer;
    private ProtobufFactory protobufFactory = new ProtobufFactoryImpl();    //пока без Autowire

    public void processTransaction(TransactionMetadata trMetadata) {
        List<LocalTask> taskList = trMetadata.getLocalTasks();
        Long transactionId = trMetadata.getTransactionId();
        System.out.println("PhstransactionId " + transactionId);
        //отправка кафке
        //быдлокод
        Set<ServicesEnum> services = taskList.stream().map(task -> task.getService())
                .collect(Collectors.toSet());
        List<String> taskNames = new ArrayList<>();
        /*taskList.forEach(task->{
            taskNames.add(task.getType().toString());
        });*/
        taskList.forEach(task->{
            Message message = null;
            message = protobufFactory.buildProtobuf(task, services);
            send(TOPICS_MAP.get(task.getService()), transactionId, message);
        });
    }

    //отправка одного сообщения
    private void send(String topic, Long transactionId, Message message) {
        //дописать переотправку и батч
        while(true)
            try {
                Future isSend = producer.send(new ProducerRecord<Long, Message>(topic, transactionId, message));
                producer.flush();
                isSend.get();
            }
            catch (Exception e) {
                System.out.println("TMStarter: send failed");
                continue;
            }



    }

    public TMStarterImpl() {
        //инициализация продюсера
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


