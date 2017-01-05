package ru.splat;


import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.Kafka.Kafka;
import ru.splat.repository.ExactlyOnceRepository;


/**
 * Created by nkalugin on 1/5/17.
 */
public class AbstractServiceFacade<KafkaRecord extends Message, InternalTrType extends TransactionRequest>
{
    private ExactlyOnceRepository exactlyOnceRepository;

    private BusinessService<InternalTrType> businessService;

    private Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter;

    private Executor thread = Executors.newSingleThreadExecutor();

    private Kafka<KafkaRecord> kafka;

    private String reqTopic;

    private String replyTopic;

    @PostConstruct
    // этот метод надо реализовывать отдельно в каждом не абстракте
    // каждый не абстракт должен иметь свою реализацию TransactionRequest
    void init()
    {
        // ну тут джаву разнесло к чертям с её системой типов :)
        // чисто ради примера, как с этим работать...
        converter = consumerRecord -> (InternalTrType) ((TransactionRequest) () -> consumerRecord.key());
        // так делать не надо. чисто пример. инстанс известного типа сообщения создать не сложно
        Message defaultInstanceForType = ((AbstractMessage) KafkaRecord).getDefaultInstanceForType();
        kafka.init(defaultInstanceForType, reqTopic);
        thread.execute(this::mainProcess);
    }


    private void mainProcess()
    {
        while (!Thread.interrupted())
        {
            try
            {
                // читаем
                ConsumerRecords<Long, KafkaRecord> consumerRecords = kafka.getConsumer().poll(1000);
                // обрабатываем с учётом идемпотентности
                List<TransactionResult> transactionResults = customProcessMessage(consumerRecords, converter);
                // пишем в кафку
                transactionResults.forEach(res ->
                        kafka.getProducer().send(new ProducerRecord<Long, Message>(replyTopic, res.getResult()))
                );
                // коммитим
                kafka.getConsumer().commitSync();
            }
            catch (Exception e)
            {
                TopicPartition partition = new TopicPartition(reqTopic, 0);
                kafka.getConsumer().seek(partition, kafka.getConsumer().committed(partition).offset());
            }
        }
    }


    // весь код надо проверять на мелкие баги. ни разу не запускался.
    @Transactional
    private List<TransactionResult> customProcessMessage(ConsumerRecords<Long, KafkaRecord> records,
            Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter)
    {
        // эта логика вообще никак не зависит от того что делает плагин.
        List<InternalTrType> transactionRequests = new ArrayList<>();
        for (ConsumerRecord<Long, KafkaRecord> record : records)
        {
            transactionRequests.add(converter.apply(record));
        }

        List<TransactionResult> readyTransactions = exactlyOnceRepository
                .filterByTable(transactionRequests.stream()
                        .map(TransactionRequest::getTrId)
                        .collect(Collectors.toList())
                );

        Set<Long> readyTransactionIds = readyTransactions.stream()
                .map(TransactionResult::getTransactionId)
                .collect(Collectors.toSet());

        List<InternalTrType> transactionsToStart = transactionRequests.stream()
                .filter(tr -> !readyTransactionIds.contains(tr.getTrId()))
                .collect(Collectors.toList());

        // тут вся специфика производимой операции.
        // единственный нужный аналог runTasks из всех методов ServiceFacade
        List<TransactionResult> transactionResults = businessService.processTransactions(transactionsToStart);

        // добрасываем идемпотентность
        exactlyOnceRepository.insertFilterTable(transactionResults);

        readyTransactions.addAll(transactionResults);
        return readyTransactions;
    }


    @Required
    public void setExactlyOnceRepository(ExactlyOnceRepository exactlyOnceRepository)
    {
        this.exactlyOnceRepository = exactlyOnceRepository;
    }


    @Required
    public void setBusinessService(BusinessService<InternalTrType> businessService)
    {
        this.businessService = businessService;
    }


    @Required
    public void setReqTopic(String reqTopic)
    {
        this.reqTopic = reqTopic;
    }


    @Required
    public void setReplyTopic(String replyTopic)
    {
        this.replyTopic = replyTopic;
    }
}
