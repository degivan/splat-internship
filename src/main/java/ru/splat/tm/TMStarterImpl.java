package ru.splat.tm;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.protobuf.*;
import ru.splat.trmetadata.*;

import java.util.*;

/**
 * Created by Дмитрий on 11.12.2016.
 */
//класс, отправляющий таски во входящие топики сервисов
    //возможно, стоит добавить отправку сообщений пачкой
public class TMStarterImpl implements TMStarter {
    KafkaProducer<Long, Message> producer;


    public void processTransaction(TransactionMetadata trMetadata) {
        List<LocalTask> taskList = trMetadata.getLocalTasks();
        Long transactionId = ((TransactionMetadata) trMetadata).getTransactionId();
        System.out.println("PhstransactionId " + transactionId);
        //отправка кафке
        //быдлокод
        List<String> taskNames = new ArrayList<>();
        taskList.forEach(task->{
            taskNames.add(task.getType().toString());
        });
        taskList.forEach(task->{
            try {
                writeToKafka(task.getService(), transactionId, ProtobufBuilder.buildProtobuf(transactionId, task, taskNames));
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }


    private void writeToKafka(String topic, Long transactionId,  Message message) {
        ProducerRecord<Long, Message> producerRecord = new ProducerRecord<Long, Message>(topic, transactionId, message);
        //дописать переотправку и батч
        producer.send(new ProducerRecord<Long, Message>(topic, transactionId, message));
        producer.flush();
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


}


