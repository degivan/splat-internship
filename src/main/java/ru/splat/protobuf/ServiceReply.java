package ru.splat.protobuf;

import com.google.protobuf.Message;

/**
 * Created by Дмитрий on 09.01.2017.
 */
public class ServiceReply {
    final private Long transactionId;
    final private Message message;

    public ServiceReply(Long transactionId, Message message) {
        this.transactionId = transactionId;
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Message getMessage() {
        return message;
    }


}
