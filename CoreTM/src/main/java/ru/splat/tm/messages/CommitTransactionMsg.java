package ru.splat.tm.messages;

import ru.splat.messages.conventions.ServicesEnum;

import java.util.Set;

/**
 * Created by Дмитрий on 20.02.2017.
 */
public class CommitTransactionMsg {
    private final long transactionId;
    private final Set<ServicesEnum> services;

    public CommitTransactionMsg(long transactionId, Set<ServicesEnum> services) {
        this.transactionId = transactionId;
        this.services = services;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public Set<ServicesEnum> getServices() {
        return services;
    }
}
