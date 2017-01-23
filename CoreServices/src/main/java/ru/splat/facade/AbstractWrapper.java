package ru.splat.facade;

import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import ru.splat.facade.feautures.TransactionRequest;
import ru.splat.kafka.Kafka;
import ru.splat.kafka.feautures.TransactionResult;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Function;
import static org.slf4j.LoggerFactory.getLogger;

//TODO проставить везде логгирование

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
                // обрабатываем с учётом идемпотентности
                List<TransactionResult> transactionResults = service.customProcessMessage(consumerRecords, converter);
                kafka.writeToKafka(transactionResults);
                kafka.commitKafka();;
            }
            catch (Exception e)
            {
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
                e.printStackTrace();
            }

        }
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
