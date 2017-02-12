package ru.splat.facade.service;

import java.util.*;
import java.util.stream.Collectors;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.facade.repository.ExactlyOnceRepositoryInterface;
import ru.splat.facade.feautures.TransactionRequest;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.facade.business.BusinessService;

import static org.slf4j.LoggerFactory.getLogger;

public class ServiceFacade<KafkaRecord extends Message, InternalTrType extends TransactionRequest>
{
    private Logger LOGGER;

    private ExactlyOnceRepositoryInterface<TransactionResult> exactlyOnceRepository;

    private BusinessService<InternalTrType> businessService;

    @Transactional
    public List<TransactionResult> customProcessMessage(Set<InternalTrType> transactionRequests) throws RuntimeException
    {

        if (transactionRequests == null || transactionRequests.isEmpty()) return null;

        LOGGER.info("Batch after set-filter");
        LOGGER.info(Arrays.toString(transactionRequests.toArray()));

        LOGGER.info("Batch from Idemp");
        List<TransactionResult> readyTransactions = exactlyOnceRepository
                .filterByTable(transactionRequests.stream()
                        .map(TransactionRequest::getTransactionId)
                        .collect(Collectors.toList())
                );
        LOGGER.info(Arrays.toString(readyTransactions.toArray()));

        Set<Long> readyTransactionIds = readyTransactions.stream()
                .map(TransactionResult::getTransactionId)
                .collect(Collectors.toSet());

        List<InternalTrType> transactionsToStart = transactionRequests.stream()
                .filter(tr -> !readyTransactionIds.contains(tr.getTransactionId()))
                .collect(Collectors.toList());

        LOGGER.info("Batch for Business Service");
        LOGGER.info(Arrays.toString(transactionsToStart.toArray()));
        // Бизнес-логика плагина
        List<TransactionResult> transactionResults = businessService.processTransactions(transactionsToStart);

        // добрасываем идемпотентность
        exactlyOnceRepository.insertFilterTable(transactionResults);

        readyTransactions.addAll(transactionResults);
//        if (readyTransactions.size() == 1) throw new RuntimeException();

        LOGGER.info("Completed transaction");
        LOGGER.info(Arrays.toString(readyTransactions.toArray()));
        return readyTransactions;
    }

    public void commitService()
    {
        businessService.commitBusinessService();
    }

    public void rollbackService()
    {
        businessService.rollbackBusinessSerivce();
    }


    @Required
    public void setExactlyOnceRepository(ExactlyOnceRepositoryInterface exactlyOnceRepository)
    {
        this.exactlyOnceRepository = exactlyOnceRepository;
    }


    @Required
    public void setBusinessService(BusinessService<InternalTrType> businessService)
    {
        this.businessService = businessService;
        LOGGER = getLogger(ServiceFacade.class);
    }
}
