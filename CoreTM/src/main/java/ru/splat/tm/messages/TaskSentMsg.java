package ru.splat.tm.messages;

import ru.splat.messages.conventions.ServicesEnum;

/**
 * Created by Дмитрий on 06.02.2017.
 */
public class TaskSentMsg {
    private final Long transactionId;
    private final ServicesEnum service;

    public TaskSentMsg(Long transactionId, ServicesEnum service) {
        this.transactionId = transactionId;
        this.service = service;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public ServicesEnum getService() {
        return service;
    }
}
