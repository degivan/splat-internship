package ru.splat;

import com.google.common.base.Throwables;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.Kafka.Kafka;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;



public interface ServiceFacade<T,V extends TransactionRequest,X> {

    String FIRST_PHASE = "phase1";
    String SECOND_PHASE = "phase2";
    String CANCEL_PHASE = "cancel";

    Logger LOGGER = getLogger(ServiceFacade.class);



    default void mainProcess(Kafka<T,V> kafka, String topic)  {
        while (true) {
            try {
                ConsumerRecords<Long, T> consumerRecords = kafka.getConsumer().poll(1);
                Map<String, Set<TransactionResult>> map = processMessage(consumerRecords);
                //sendResult(map);
                kafka.getConsumer().commitSync();
            } catch (Exception e) {
                TopicPartition partition = new TopicPartition(topic, 0);
                kafka.getConsumer().seek(partition, kafka.getConsumer().committed(partition).offset());
                System.out.println("sas");
                Throwables.propagate(e);
            }


        }
    }


    void writeIdemp(Map<String, Set<TransactionResult>> results);

    void sendResult(Map<String, Set<TransactionResult>> map);

    @Transactional
    Map<String,Set<TransactionResult>> processMessage(ConsumerRecords<Long, T> consumerRecords) throws Exception;

    Map<String, Set<TransactionResult>> runTasks(Map<String, Set<X>> filter);

    Map filterSeen(ConsumerRecords<Long, T> consumerRecords);

    // public abstract Map<String,Set<TransactionResult>> process();
}
