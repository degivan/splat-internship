package ru.splat.tm.messages;

/**
 * Created by Дмитрий on 20.02.2017.
 */
public class CommitTransactionMsg {
    private final long transactionId;

    public CommitTransactionMsg(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getTransactionId() {
        return transactionId;
    }
}
