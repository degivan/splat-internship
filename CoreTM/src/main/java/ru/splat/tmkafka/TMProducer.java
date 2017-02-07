package ru.splat.tmkafka;

import com.google.protobuf.Message;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.tm.RequestContainer;

import java.util.List;

/**
 * Created by Дмитрий on 06.01.2017.
 */
public interface TMProducer {
    void send(String topic, Long transactionId, Message message);
    void sendBatch(List<RequestContainer> requests);

}
