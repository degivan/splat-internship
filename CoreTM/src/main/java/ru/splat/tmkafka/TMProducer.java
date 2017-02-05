package ru.splat.tmkafka;

import com.google.protobuf.Message;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;

import java.util.List;

/**
 * Created by Дмитрий on 06.01.2017.
 */
public interface TMProducer {
    void write(Long transactionId, Message message);
    void writeBatch(List<TransactionMetadata> transactionMetadatas);

}
