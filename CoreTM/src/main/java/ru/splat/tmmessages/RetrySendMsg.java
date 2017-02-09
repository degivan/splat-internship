package ru.splat.tmmessages;

import com.google.protobuf.Message;

/**
 * Created by Дмитрий on 09.02.2017.
 */
public class RetrySendMsg {
    private final String topic;
    private final Long transactionId;
    private final Message message;

    public RetrySendMsg(String topic, Long transactionId, Message message) {
        this.topic = topic;
        this.transactionId = transactionId;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Message getMessage() {
        return message;
    }
}
