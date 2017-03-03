package ru.splat.tm.actors;

import akka.event.LoggingAdapter;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Дмитрий on 03.03.2017.
 */
public class TopicTracker {
    private Map<Long, Long> records = new HashMap<>();
    private final String topicName;
    private final TopicPartition partition;
    private long currentOffset;   //текущий коммитабельный оффсет консюмера
    private Set<Long> commitedTransactions= new HashSet<>();
    private final LoggingAdapter log;

    long getCurrentOffset() {
        return currentOffset;
    }

    private TopicTracker(TopicPartition partition, long currentOffset, LoggingAdapter log) {
        this.topicName = partition.topic();
        this.partition = partition;
        this.currentOffset = currentOffset;
        this.log = log;
    }
    String getTopicName() {
        return topicName;
    }
    //возрващает true, если запись уже встречалась
    boolean addRecord(long offset, long trId) {
        if (records.containsValue(trId)) {
            records.put(offset, -1L);
            return true;
        }   //trId -1 - индикатор лишнего сообщения (можно коммитить)
        else {
            records.put(offset, trId);
            return false;
        }
        //log.info(topicName + ": record with id " + trId);
    }
    //возвращает оффсет (абсолютный) до которого можно коммитить или -1, если коммитить пока нельзя
    void commitTransaction(long trId) {
        commitedTransactions.add(trId); //добавляем эту транзакцию в закоммиченные
        log.info(topicName + ": currentOffset:  " + currentOffset + ". Commit request " + trId); //StringBuilder sb = new StringBuilder();
        //records.entrySet().forEach(entry -> sb.append(entry.getKey() + " : " + entry.getValue() + " | ")); log.info(sb.toString());
        long offset = currentOffset;
        boolean commitable = false;
        while(true) {
            Long record = records.get(offset);
            if (record == null || !(commitedTransactions.contains(record) || record == -1)) break;
            else {
                offset++;
                if (record == trId)
                    commitable = true;
            }
        }
        if (commitable) {
            commitedTransactions.remove(trId);
            while (currentOffset < offset) {    //перемещаем currentOffset на актулальную позицию
                records.remove(currentOffset);
                currentOffset++;
            }
            log.info(topicName + "Tracker is now at offset: " + currentOffset);
        }
    }
    //make excess transaction message commitable
    void markTransaction(long offset) {
        //log.info("excess message is caught!!! offset: " + offset + " topic: " + topicName); //for testing
        if (records.containsKey(offset))
            records.put(offset, -1L);
    }

    TopicPartition getPartition() {
        return partition;
    }
}
