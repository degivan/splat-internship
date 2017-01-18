package ru.splat.facade.kafka;


import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.facade.feautures.TransactionResult;
import ru.splat.facade.protobuf.ProtoBufMessageDeserializer;
import ru.splat.facade.protobuf.ProtoBufMessageSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class KafkaImpl<ProtobufRequest extends Message> implements Kafka<ProtobufRequest>
{
    private final KafkaProducer<Long, Message> producer;
    private final KafkaConsumer<Long, ProtobufRequest> consumer;
    private final String TOPIC_REQUEST;
    private final String TOPIC_RESPONSE;
    private static long currentOffset = 0;


    public KafkaImpl(String producerTopic, String consumerTopic, Message defaultInstance)
    {
        this.TOPIC_REQUEST = consumerTopic;
        this.TOPIC_RESPONSE = producerTopic;
        Properties propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "false");
        // propsConsumer.put("session.timeout.ms", "30000");


        consumer = new KafkaConsumer(propsConsumer, new LongDeserializer(), new ProtoBufMessageDeserializer(defaultInstance));
        consumer.subscribe(Arrays.asList(TOPIC_REQUEST));

        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);

        producer = new KafkaProducer(propsProducer, new LongSerializer(), new ProtoBufMessageSerializer());

    }



    @Override
    public ConsumerRecords<Long, ProtobufRequest> readFromKafka(long timeout)
    {
        if (consumer != null)
        {
            ConsumerRecords<Long,ProtobufRequest> consumerRecords = consumer.poll(timeout);
            currentOffset = consumerRecords.count();
            return consumerRecords;
        }

        return null;
    }


    @Override
    public void resetConsumerToCommitedOffset()
    {
        if (consumer!=null)
        {
            TopicPartition partition = new TopicPartition(TOPIC_REQUEST, 0);
            consumer.seek(partition, consumer.committed(partition).offset()-currentOffset);
        }
    }

    @Override
    public void writeToKafka(List<TransactionResult> transactionResults) {

        if (transactionResults == null) return;

        List<Future> futureList = new ArrayList<>();
        while (!transactionResults.isEmpty())
        {
            futureList = transactionResults.stream().map(map ->
                    producer.send(new ProducerRecord<Long, Message>(TOPIC_RESPONSE,map.getTransactionId(), map.getResult())))
                    .collect(Collectors.toList());
            producer.flush();

            for (int i = 0; i < futureList.size(); i++)
            {
                try
                {
                    futureList.get(i).get();
                    transactionResults.remove(i);
                } catch (Exception e)
                {
                }
            }
        }
        currentOffset = 0;
    }




    @Override
    public void commitKafka()
    {
        if (consumer!=null)
        {
            consumer.commitSync();
            currentOffset = 0;
        }
    }
}
