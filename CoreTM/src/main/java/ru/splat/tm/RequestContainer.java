package ru.splat.tm;

import com.google.protobuf.Message;

/**
 * Created by Дмитрий on 06.02.2017.
 */
public class RequestContainer {
    private final Long transactionId;
    private final String topic;
    private final Message message;

    public RequestContainer(Long transactionId, String topic, Message message) {
        this.transactionId = transactionId;
        this.topic = topic;
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getTopic() {
        return topic;
    }

    public Message getMessage() {
        return message;
    }
}
