package ru.splat.facade;


import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
import ru.splat.facade.feautures.TransactionResult;
import ru.splat.facade.kafka.Kafka;
import ru.splat.facade.business.BusinessService;

import static org.slf4j.LoggerFactory.getLogger;


public abstract class AbstractServiceFacade<KafkaRecord extends Message, InternalTrType extends TransactionRequest>
{
    Logger LOGGER = getLogger(AbstractServiceFacade.class);

    private ExactlyOnceRepositoryInterface exactlyOnceRepository;

    private BusinessService<InternalTrType> businessService;

    private Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter;

    private Executor thread = Executors.newSingleThreadExecutor();

    private Kafka<KafkaRecord> kafka;

    private long consumerTimeout;

    public void startThread()
    {
        thread.execute(this::mainProcess);
    }

    public void setConsumerTimeout(long consumerTimeout)
    {
        this.consumerTimeout = consumerTimeout;
    }

    @PostConstruct
    public abstract void init();


    public void mainProcess()
    {
        while (!Thread.interrupted())
        {
            try
            {
                // читаем
                ConsumerRecords<Long, KafkaRecord> consumerRecords = kafka.readFromKafka(consumerTimeout);
                // обрабатываем с учётом идемпотентности
                List<TransactionResult> transactionResults = customProcessMessage(consumerRecords, converter);
                kafka.writeToKafka(transactionResults);
                kafka.commitKafka();
            }
            catch (Exception e)
            {
                LOGGER.error("High level",e);
                kafka.resetConsumerToCommitedOffset();
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    @Transactional
    public List<TransactionResult> customProcessMessage(ConsumerRecords<Long, KafkaRecord> records,
            Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter)
    {
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


        LOGGER.info("Data for idempoty");
        // добрасываем идемпотентность
        exactlyOnceRepository.insertFilterTable(transactionResults);

        readyTransactions.addAll(transactionResults);

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


    public void setConverter(Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter)
    {
        this.converter = converter;
    }

    public void setKafka(Kafka<KafkaRecord> kafka)
    {
        this.kafka = kafka;
    }
}
