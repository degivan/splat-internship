package ru.splat.messages.uptm;

import ru.splat.messages.uptm.trstate.TransactionState;

import java.util.function.Consumer;

/**
 * Created by Дмитрий on 20.02.2017.
 */
public class TransactionStateMsg {
    private final TransactionState transactionState;
    private final Consumer<Void> commitTransaction;

    public TransactionStateMsg(TransactionState transactionState, Consumer<Void> commitTransaction) {
        this.transactionState = transactionState;
        this.commitTransaction = commitTransaction;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public Consumer<Void> getCommitTransaction() {
        return commitTransaction;
    }
}
