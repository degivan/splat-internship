package ru.splat.tmkafka;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.kafka.serializer.ProtoBufMessageSerializer;
import ru.splat.tm.RequestContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by Дмитрий on 06.01.2017.
 */
public class TMProducerImpl implements TMProducer {
    private KafkaProducer<Long, Message> producer;

    public TMProducerImpl() {
        //инициализация продюсера
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
    public void send(String topic, Long transactionId, Message message) {
        ProducerRecord<Long, Message> pr = new ProducerRecord<Long, Message>(topic, transactionId, message);
        //producer.send(pr);
        //дописать переотправку и батч
        while(true)
            try {
                Future isSend = producer.send(new ProducerRecord<Long, Message>(topic, transactionId, message));
                producer.flush();
                isSend.get();
                break;
            }
            catch (Exception e) {
                System.out.println("TMStarter: send failed");
                continue;
            }
    }

    @Override
    public void sendBatch(List<RequestContainer> requests) {
        if (requests == null) return;

        List<Future> futureList = new ArrayList<>();
        while (!requests.isEmpty())
        {
            futureList = requests.stream().map(request ->
                    producer.send(new ProducerRecord<Long, Message>(request.getTopic(), request.getTransactionId(), request.getMessage())))
                    .collect(Collectors.toList());
            producer.flush();

            for (int i = 0; i < futureList.size(); i++)
            {
                try
                {
                    futureList.get(i).get();
                    requests.remove(i);
                } catch (Exception e)
                {
                }
            }
        }
    }
}
