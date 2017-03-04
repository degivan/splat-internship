package ru.splat.tm.messages;

/**
 * Created by Дмитрий on 04.03.2017.
 */
public class TMCommitTransactionMsg {
    private final long transactionId;

    public TMCommitTransactionMsg(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getTransactionId() {
        return transactionId;
    }
}
