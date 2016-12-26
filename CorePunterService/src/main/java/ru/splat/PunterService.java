package ru.splat;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.Shedule.SheduleCleaningDB;
import ru.splat.feautures.BetInfo;

import ru.splat.protobuf.PunterReq;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import ru.splat.protobuf.PunterRes;

@Service
public class PunterService extends ServiceFacade<PunterReq.Punter, PunterRes.Person,BetInfo> {

    private static final String TOPIC_REQUEST = "mytopic3";
    private static final String TOPIC_RESPONSE = "mytopic4";


    public static void main(String[] args) throws IOException, InterruptedException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-core.xml");
        PunterService punterService = context.getBean(PunterService.class);

        topic = "mytopic3";
        punterService.init(PunterReq.Punter.getDefaultInstance());
        //punterService.consumer.poll(1);
        //punterService.consumer.commitSync();
         punterService.mainProcess();


    }

    @Override
    protected void sendResult(Map<String, Set<TransactionResult>> map) {
        for (Map.Entry<String, Set<TransactionResult>> entry : map.entrySet()) {
            for (TransactionResult transactionResult : entry.getValue()) {
                PunterRes.Person pr = PunterRes.Person.newBuilder().setTransactionID(transactionResult.getTransactionId()).setResult(transactionResult.getResult()).build();
               // producer.send(new ProducerRecord<Long, PunterRes.Person>(TOPIC_RESPONSE, 1l, pr));
            }
        }
    }

    @Override
    @Transactional
    public Map<String, Set<TransactionResult>> processMessage(ConsumerRecords<Long, PunterReq.Punter> consumerRecords) throws SQLException {


        Map<String, Set<BetInfo>> afterFirstFilter = filterSeen(consumerRecords);

        Map<String, Set<TransactionResult>> results = new HashMap<>();
        List<TransactionResult> intermediateTransactionResults;

        for (Map.Entry<String, Set<BetInfo>> entry : afterFirstFilter.entrySet()) {
            intermediateTransactionResults = idempRepository.filterByTable(new ArrayList<BetInfo>(entry.getValue()));

            Set<Long> intermediateResults2 = new HashSet<>();
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

        if (!results2.isEmpty()) throw new RuntimeException();
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
            idempRepository.insertFilterTable(entry.getValue().stream().collect(Collectors.toList()));
        }
    }

    @Override
    protected Map<String, Set<TransactionResult>> runTasks(Map<String, Set<BetInfo>> filter) {
        Map<String, Set<TransactionResult>> result = new HashMap<>();
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
    protected Map<String, Set<BetInfo>> filterSeen(ConsumerRecords<Long, PunterReq.Punter> consumerRecords) {
        Map<String, Set<BetInfo>> filter = new HashMap<>();
        for (ConsumerRecord<Long, PunterReq.Punter> record : consumerRecords) {
            if (!filter.containsKey(record.value().getLocalTask())) {
                filter.put(record.value().getLocalTask(), new HashSet<>());
            }
            filter.get(record.value().getLocalTask()).add(new BetInfo(record.value().getPunterId(), record.key()));
        }
        return filter;
    }

}
