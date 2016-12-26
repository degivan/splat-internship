package ru.splat.Billing;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.Billing.protobuf.BallanceReq;
import ru.splat.Billing.protobuf.BallanceRes;
import ru.splat.Billing.repository.BillingIdempRepository;
import ru.splat.Billing.repository.BillingRepository;
import ru.splat.ServiceFacade;
import ru.splat.feautures.BetInfo;
import ru.splat.protobuf.PunterReq;
import ru.splat.repository.IdempRepositoryInterface;
import ru.splat.repository.PunterRepository;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillingService extends ServiceFacade<BallanceReq.Billing,BallanceRes.Billing,BillingInfo>
{

    @Autowired
    BillingIdempRepository idempRepository;

    @Autowired
    BillingRepository billingRepository;


    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-core.xml");
        BillingService billingService = applicationContext.getBean(BillingService.class);
        topic = "BillingIN";
        billingService.init(BallanceReq.Billing.getDefaultInstance());
        billingService.mainProcess();
      //  billingService.consumer.poll(10);
      //  billingService.consumer.commitSync();
    }

    @Override
    protected void sendResult(Map map) {

    }

    @Override
    @Transactional
    protected Map<String, Set<TransactionResult>> processMessage(ConsumerRecords<Long, BallanceReq.Billing> consumerRecords) {
        if (consumerRecords == null || consumerRecords.isEmpty()) return null;
        Map<String, Set<BillingInfo>> afterFirstFilter = filterSeen(consumerRecords);

        Map<String, Set<TransactionResult>> results = new HashMap<>();
        List<TransactionResult> intermediateTransactionResults;

        for (Map.Entry<String, Set<BillingInfo>> entry : afterFirstFilter.entrySet()) {
            intermediateTransactionResults = idempRepository.filterByTable(new ArrayList<BillingInfo>(entry.getValue()));

            Set<Long> intermediateResults2 = new HashSet<>();
            for (TransactionResult transactionResult : intermediateTransactionResults) {
                intermediateResults2.add(transactionResult.getTransactionId());
            }
            Iterator<BillingInfo> it = entry.getValue().iterator();
            while (it.hasNext()) {
                BillingInfo billingInfo = it.next();
                if (intermediateResults2.contains(billingInfo.getTransactionId())) {
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
            idempRepository.insertFilterTable(entry.getValue().stream().collect(Collectors.toList()));
        }
    }

    @Override
    protected Map<String,Set<BillingInfo>> filterSeen(ConsumerRecords<Long,BallanceReq.Billing> consumerRecords) {
        Map<String, Set<BillingInfo>> filter = new HashMap<>();
        for (ConsumerRecord<Long, BallanceReq.Billing> record : consumerRecords) {
            if (!filter.containsKey(record.value().getLocalTask())) {
                filter.put(record.value().getLocalTask(), new HashSet<>());
            }
            filter.get(record.value().getLocalTask()).add(new BillingInfo(record.value().getPunterId(), record.value().getBallance(),record.key()));
        }
        return filter;
    }

    @Override
    protected Map<String, Set<TransactionResult>> runTasks(Map<String, Set<BillingInfo>> filter) {
        Map<String, Set<TransactionResult>> result = new HashMap<>();
        for (Map.Entry<String, Set<BillingInfo>> entry : filter.entrySet()) {
            switch (entry.getKey()) {
                case FIRST_PHASE:
                    result.put(FIRST_PHASE, billingRepository.phase1(new ArrayList<BillingInfo>(entry.getValue())));
                    break;
                case CANCEL_PHASE:
                    result.put(CANCEL_PHASE, billingRepository.cancel(new ArrayList<BillingInfo>(entry.getValue())));
                    break;
            }
        }
        return result;
    }
}
