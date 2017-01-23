package ru.splat.facade;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
    private Logger LOGGER = getLogger(ServiceFacade.class);

    private ExactlyOnceRepositoryInterface<TransactionResult> exactlyOnceRepository;

    private BusinessService<InternalTrType> businessService;

    //TODO по хорошему надо избавиться здесь от ConsumerRecords
    @Transactional
    public List<TransactionResult> customProcessMessage(ConsumerRecords<Long, KafkaRecord> records,
            Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter) throws Exception {
        if (records==null || records.isEmpty()) return null;

        Set<InternalTrType> transactionRequests = new HashSet<>();
        LOGGER.info("Batch from Kafka");
        for (ConsumerRecord<Long, KafkaRecord> record : records)
        {
            transactionRequests.add(converter.apply(record));
            LOGGER.info("Transaction ID: " + record.key());
            LOGGER.info(record.value().toString());
        }

        List<TransactionResult> readyTransactions = exactlyOnceRepository
                .filterByTable(transactionRequests.stream()
                        .map(TransactionRequest::getTransactionId)
                        .collect(Collectors.toList())
                );

        LOGGER.info("Batch after filter");
        LOGGER.info(Arrays.toString(readyTransactions.toArray()));

        Set<Long> readyTransactionIds = readyTransactions.stream()
                .map(TransactionResult::getTransactionId)
                .collect(Collectors.toSet());

        List<InternalTrType> transactionsToStart = transactionRequests.stream()
                .filter(tr -> !readyTransactionIds.contains(tr.getTransactionId()))
                .collect(Collectors.toList());

        // Бизнес-логика плагина
        List<TransactionResult> transactionResults = businessService.processTransactions(transactionsToStart);

        // добрасываем идемпотентность
        exactlyOnceRepository.insertFilterTable(transactionResults);

        readyTransactions.addAll(transactionResults);
//        throw new RuntimeException();

        LOGGER.info("Completed transaction");
        LOGGER.info(Arrays.toString(readyTransactions.toArray()));
        return readyTransactions;
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
    }
}
