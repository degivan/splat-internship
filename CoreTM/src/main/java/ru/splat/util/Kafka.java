package ru.splat.util;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import ru.splat.Punter.feautures.TransactionResult;

import java.util.List;


public interface Kafka<T>
{
    ConsumerRecords<Long, T> readFromKafka(long timeout);
    void writeToKafka(List<TransactionResult> transactionResults);
    void resetConsumerToCommitedOffset();
    void commitKafka();
    void init();
}
