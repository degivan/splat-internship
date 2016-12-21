package ru.splat;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.feautures.BetInfo;
import ru.splat.feautures.TransactionResult;
import ru.splat.protobuf.PunterReq;
import ru.splat.protobuf.PunterRes;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PunterService extends ServiceFacade<PunterReq.Person, PunterRes.Person> {

    private static final String TOPIC_REQUEST = "mytopic3";
    private static final String TOPIC_RESPONSE = "mytopic4";


    public static void main(String[] args) throws IOException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-core.xml");
        PunterService punterService = context.getBean(PunterService.class);

        punterService.init(PunterReq.Person.getDefaultInstance(), TOPIC_REQUEST);
        // punterService.consumer.commitSync();

        while (true) {
            try {
                ConsumerRecords<String, PunterReq.Person> consumerRecords = punterService.consumer.poll(10);
                Map<String, Set<TransactionResult>> map = punterService.processMessage(consumerRecords);
                // punterService.sendResult(map, punterService.producer);
                punterService.consumer.commitSync();
            } catch (Exception e) {
                TopicPartition partition = new TopicPartition(TOPIC_REQUEST, 1);
                punterService.consumer.seek(partition, punterService.consumer.committed(partition).offset());
            }
        }

    }

    private void sendResult(Map<String, Set<TransactionResult>> map, KafkaProducer producer) {
        for (Map.Entry<String, Set<TransactionResult>> entry : map.entrySet()) {
            for (TransactionResult transactionResult : entry.getValue()) {
                PunterRes.Person pr = PunterRes.Person.newBuilder().setLocalTask(entry.getKey()).setTransactionID(transactionResult.getTransactionId()).setResult(transactionResult.getResult()).build();
                producer.send(new ProducerRecord<String, PunterRes.Person>(TOPIC_RESPONSE, entry.getKey(), pr));
            }
        }
    }

    @Override
    @Transactional
    protected Map<String, Set<TransactionResult>> processMessage(ConsumerRecords<String, PunterReq.Person> consumerRecords) {
        Map<String, Set<BetInfo>> afterFirstFilter = filterSeen(consumerRecords);

        HashMap<String, Set<TransactionResult>> results = new HashMap<>();
        ArrayList<TransactionResult> intermediateTransactionResults;

        for (Map.Entry<String, Set<BetInfo>> entry : afterFirstFilter.entrySet()) {
            intermediateTransactionResults = (ArrayList<TransactionResult>) punterRepository.filterByTable(new ArrayList<BetInfo>(entry.getValue()), entry.getKey());

            HashSet<Long> intermediateResults2 = new HashSet<>();
            for (TransactionResult transactionResult : intermediateTransactionResults) {
                intermediateResults2.add(transactionResult.getTransactionId());
            }
            Iterator<BetInfo> it = entry.getValue().iterator();
            while (it.hasNext()) {
                BetInfo betInfo = it.next();
                if (intermediateResults2.contains(betInfo.getTransactionId())) {
                    it.remove();
                }
            }
            results.put(entry.getKey(), new HashSet<TransactionResult>(intermediateTransactionResults));
        }


        Iterator it = afterFirstFilter.keySet().iterator();
        while (it.hasNext()) {
            if (afterFirstFilter.get(it.next()).isEmpty())
                it.remove();
        }


        Map<String, Set<TransactionResult>> results2 = runTasks(afterFirstFilter);


        writeIdemp(results2);
        for (Map.Entry<String, Set<TransactionResult>> entry : results.entrySet()) {
            if (results2.containsKey(entry.getKey()))
                results2.get(entry.getKey()).retainAll(entry.getValue());
            else if (!entry.getValue().isEmpty())
                results2.put(entry.getKey(), entry.getValue());
        }
        return results2;
    }

    private void writeIdemp(Map<String, Set<TransactionResult>> results) {
        if (results == null || results.isEmpty()) return;
        for (Map.Entry<String, Set<TransactionResult>> entry : results.entrySet()) {
            punterRepository.insertFilterTable(entry.getValue().stream().collect(Collectors.toList()), entry.getKey());
        }
    }

    @Override
    protected Map<String, Set<TransactionResult>> runTasks(Map<String, Set<BetInfo>> filter) {
        HashMap<String, Set<TransactionResult>> result = new HashMap<>();
        for (Map.Entry<String, Set<BetInfo>> entry : filter.entrySet()) {
            switch (entry.getKey()) {
                case FIRST_PHASE:
                    result.put(FIRST_PHASE, punterRepository.phase1(entry.getValue()));
                    break;
                case CANCEL_PHASE:
                    result.put(CANCEL_PHASE, punterRepository.cancel(new ArrayList<BetInfo>(entry.getValue())));
                    break;
            }
        }
        return result;
    }

    @Override
    protected Map<String, Set<BetInfo>> filterSeen(ConsumerRecords<String, PunterReq.Person> consumerRecords) {
        Map<String, Set<BetInfo>> filter = new HashMap<>();
        for (ConsumerRecord<String, PunterReq.Person> record : consumerRecords) {
            if (!filter.containsKey(record.value().getLocalTask())) {
                filter.put(record.value().getLocalTask(), new HashSet<BetInfo>());
            }
            filter.get(record.value().getLocalTask()).add(new BetInfo(record.value().getPunterId(), record.value().getTrId()));
        }
        return filter;
    }

}
