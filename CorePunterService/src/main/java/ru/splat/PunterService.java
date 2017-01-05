package ru.splat;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.Kafka.PunterKafka;
import ru.splat.feautures.BetInfo;
import ru.splat.feautures.RepAnswer;
import ru.splat.protobuf.PunterReq;
import java.util.*;
import java.util.stream.Collectors;
import ru.splat.protobuf.PunterRes;
import ru.splat.repository.IdempRepositoryInterface;
import ru.splat.repository.PunterRepository;


@Transactional
public class PunterService implements ServiceFacade<PunterReq.Punter, PunterRes.Punter, BetInfo>
{
    @Autowired
    private IdempRepositoryInterface<BetInfo, TransactionResult> idempRepository;

    @Autowired
    private PunterRepository punterRepository;


    @Autowired
    private PunterKafka punterKafka;

    public static final String TOPIC_REQUEST = "mytopic3";

    @Override
    public void sendResult(Map<String, Set<TransactionResult>> map) {
        for (Map.Entry<String, Set<TransactionResult>> entry : map.entrySet()) {
            for (TransactionResult transactionResult : entry.getValue()) {
             //   PunterRes.Punter pr = PunterRes.PunternewBuilder().setTransactionID(transactionResult.getTransactionId()).setResult(transactionResult.getResult()).build();
               // producer.send(new ProducerRecord<Long, PunterRes.Person>(TOPIC_RESPONSE, 1l, pr));
            }
        }
    }

    @Override
    @Transactional
    public Map<String, Set<TransactionResult>> processMessage(ConsumerRecords<Long, PunterReq.Punter> consumerRecords)  {

        //System.out.println("Tx : " + TransactionSynchronizationManager.isActualTransactionActive());

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

        // 4.1. Поведение при Exceptions
      // if (!results2.isEmpty()) throw new RuntimeException();
        // 4.2. Поведение при отключении сервера.
       // System.exit(0);
        writeIdemp(results2);
        for (Map.Entry<String, Set<TransactionResult>> entry : results.entrySet()) {
            if (results2.containsKey(entry.getKey()))
                results2.get(entry.getKey()).retainAll(entry.getValue());
            else if (!entry.getValue().isEmpty())
                results2.put(entry.getKey(), entry.getValue());
        }
        return results2;
}

    @Override
    public void writeIdemp(Map<String, Set<TransactionResult>> results) {
        if (results == null || results.isEmpty()) return;
        for (Map.Entry<String, Set<TransactionResult>> entry : results.entrySet()) {
            idempRepository.insertFilterTable(entry.getValue().stream().collect(Collectors.toList()));
        }
    }

    //вынести в фасад?
    @Override
    public Map<String, Set<TransactionResult>> runTasks(Map<String, Set<BetInfo>> filter) {
        Map<String, Set<TransactionResult>> result = new HashMap<>();
        for (Map.Entry<String, Set<BetInfo>> entry : filter.entrySet()) {
            switch (entry.getKey()) {
                case FIRST_PHASE:
                    result.put(FIRST_PHASE, punterRepository.phase1(entry.getValue()).stream().
                              map((map) -> new TransactionResult(map.getTransactionId(), PunterRes.Punter.newBuilder().setTransactionID(map.getTransactionId()).setResult(map.isResult()).setResultReason(map.getResultReason()).build())).collect(Collectors.toSet()));
                    break;
                case CANCEL_PHASE:
                    result.put(CANCEL_PHASE, punterRepository.cancel(new ArrayList<BetInfo>(entry.getValue())).stream().
                            map((map) -> new TransactionResult(map.getTransactionId(), PunterRes.Punter.newBuilder().setTransactionID(map.getTransactionId()).setResult(map.isResult()).setResultReason(map.getResultReason()).build())).collect(Collectors.toSet()));
                    break;
            }
        }
        return result;
    }

    @Override
    public Map<String, Set<BetInfo>> filterSeen(ConsumerRecords<Long, PunterReq.Punter> consumerRecords) {
        Map<String, Set<BetInfo>> filter = new HashMap<>();
        for (ConsumerRecord<Long, PunterReq.Punter> record : consumerRecords) {
            if (!filter.containsKey(record.value().getLocalTask())) {
                filter.put(record.value().getLocalTask(), new HashSet<>());
            }
            filter.get(record.value().getLocalTask()).add(new BetInfo(record.value().getPunterId(), record.key()));
        }
        return filter;
    }


    public PunterKafka getPunterKafka() {
        return punterKafka;
    }

}
