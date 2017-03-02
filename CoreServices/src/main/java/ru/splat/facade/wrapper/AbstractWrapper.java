package ru.splat.facade.wrapper;

import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import ru.splat.facade.service.ServiceFacade;
import ru.splat.facade.feautures.TransactionRequest;
import ru.splat.kafka.Kafka;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.messages.proxyup.bet.NewResponseClone;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractWrapper<KafkaRecord extends Message, InternalTrType extends TransactionRequest>
{

    private Logger LOGGER = getLogger(this.getClass());

    private long consumerTimeout;

    private Kafka<KafkaRecord> kafka;

    private Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter;

    private ServiceFacade<KafkaRecord, InternalTrType> service;

    @PostConstruct
    public abstract void init();

    private long offset;

    private long commitOffset;

    private List<ConsumerRecord<Long,KafkaRecord>> consumerRecordList;



    public void mainProcess()
    {

        kafka.resetConsumerToCommitedOffset();
        offset = 0;
        commitOffset = 0;

        while (!Thread.interrupted())
        {
            try {
                // читаем
                // конверитруем
                Set<InternalTrType> transactionRequest = read();
                // обрабатываем с учётом идемпотентности
                List<TransactionResult> transactionResults = service.customProcessMessage(transactionRequest);
                 // TODO подумать как завернуть через AOP
                service.commitService();
                kafka.writeToKafka(transactionResults);
                kafka.commitKafka(commitOffset);
            }
            catch (Exception e)
            {
                service.rollbackService();
                LOGGER.error("High level",e);
                while (true)
                {
                    try
                    {
                        kafka.resetConsumerToCommitedOffset();
                        break;
                    }catch (Exception ex)
                    {
                        LOGGER.error("High level",ex);
                    }
                }

            }

        }
    }

    private Set<InternalTrType> read()
    {
        if (offset == 0)
        {
            try {
                Thread.currentThread().sleep(100L);
            } catch (InterruptedException e) {
                LOGGER.error("ThreadSleep level",e);
            }

            ConsumerRecords<Long, KafkaRecord> consumerRecords = kafka.readFromKafka(consumerTimeout);
            consumerRecordList = new ArrayList<>(consumerRecords.count());
            consumerRecords.forEach(p ->consumerRecordList.add(p));
            offset = consumerRecords.count();
        }

        Set<InternalTrType> transactionRequest = new HashSet<>();
        Iterator<ConsumerRecord<Long,KafkaRecord>> iterator = consumerRecordList.iterator();
        int i = 0;
        while (iterator.hasNext() && i < 500)
        {
            ConsumerRecord<Long,KafkaRecord> consumerRecord = iterator.next();
            iterator.remove();
            transactionRequest.add(converter.apply(consumerRecord));
            LOGGER.info("Transaction id = " + consumerRecord.key());
            LOGGER.info(consumerRecord.value().toString());
            i++;
        }

        offset -= i;
        commitOffset = i;
        return transactionRequest;
    }


    public void setConsumerTimeout(long consumerTimeout)
    {
        this.consumerTimeout = consumerTimeout;
    }

    public void setConverter(Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter)
    {
        this.converter = converter;
    }

    public void setKafka(Kafka<KafkaRecord> kafka)
    {
        this.kafka = kafka;
    }

    @Required
    public void setService(ServiceFacade<KafkaRecord, InternalTrType> service) {
        this.service = service;
    }
}
