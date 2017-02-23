package ru.splat.kafka;


import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.kafka.deserializer.ProtoBufMessageDeserializer;
import ru.splat.kafka.feautures.TransactionResult;
import ru.splat.kafka.serializer.ProtoBufMessageSerializer;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class KafkaImpl<ProtobufRequest extends Message> implements Kafka<ProtobufRequest>
{
    private final KafkaProducer<Long, Message> producer;
    private final KafkaConsumer<Long, ProtobufRequest> consumer;
    private final String TOPIC_REQUEST;
    private final String TOPIC_RESPONSE;


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
        consumer.assign(Collections.singletonList(new TopicPartition(TOPIC_REQUEST,0)));
//        consumer.subscribe(Arrays.asList(TOPIC_REQUEST));

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
            consumer.seek(partition, consumer.committed(partition).offset());
        }
    }

    @Override
    public void writeToKafka(List<TransactionResult> transactionResults)
    {

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
    }




    @Override
    public void commitKafka(long offset)
    {
        if (consumer!=null && offset!= 0)
        {
            TopicPartition partition = new TopicPartition(TOPIC_REQUEST, 0);
            Map<TopicPartition,OffsetAndMetadata> map = new HashMap<>(1);
            long longOffset = (consumer.committed(partition) != null)?consumer.committed(partition).offset():0;
            OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(longOffset + offset);
            map.put(partition,offsetAndMetadata);
            consumer.commitSync(map);
        }
    }
}
