package ru.splat.tm.messages;

import ru.splat.messages.conventions.ServicesEnum;

/**
 * Created by Дмитрий on 22.02.2017.
 */

//сообщения для TMConsumerActor-а с transactionId лишнего сообщения (необходимо закоммитить)
public class MarkSpareMsg {
    private final long transactionId;
    private final ServicesEnum service;
    private final long offset;

    public MarkSpareMsg(long transactionId, ServicesEnum service, long offset) {
        this.transactionId = transactionId;
        this.service = service;
        this.offset = offset;
    }
    public long getTransactionId() {
        return transactionId;
    }
    public ServicesEnum getService() {
        return service;
    }

    public long getOffset() {
        return offset;
    }
}
