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
import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractWrapper<KafkaRecord extends Message, InternalTrType extends TransactionRequest>
{

    private Logger LOGGER = getLogger(AbstractWrapper.class);

    private long consumerTimeout;

    private Kafka<KafkaRecord> kafka;

    private Function<ConsumerRecord<Long, KafkaRecord>, InternalTrType> converter;

    private ServiceFacade<KafkaRecord, InternalTrType> service;

    @PostConstruct
    public abstract void init();


    public void mainProcess()
    {

        while (!Thread.interrupted())
        {
            try {
                // читаем
                ConsumerRecords<Long, KafkaRecord> consumerRecords = kafka.readFromKafka(consumerTimeout);
                // конверитруем
                Set<InternalTrType> transactionRequest = convertToSet(consumerRecords);
                // обрабатываем с учётом идемпотентности
                List<TransactionResult> transactionResults = service.customProcessMessage(transactionRequest);
                 // TODO подумать как завернуть через AOP
                service.commitService();
                kafka.writeToKafka(transactionResults);
                kafka.commitKafka();
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

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                LOGGER.error("ThreadSleep level",e);
            }

        }
    }

    private Set<InternalTrType> convertToSet(ConsumerRecords<Long, KafkaRecord> consumerRecords)
    {
        Set<InternalTrType> transactionRequest = new HashSet<>();
        for (ConsumerRecord<Long, KafkaRecord> consumerRecord: consumerRecords)
        {
            transactionRequest.add(converter.apply(consumerRecord));
            LOGGER.info("Transaction id = " + consumerRecord.key());
            LOGGER.info(consumerRecord.value().toString());
        }

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
