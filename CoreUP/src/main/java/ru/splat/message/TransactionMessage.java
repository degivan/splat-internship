package ru.splat.message;

import ru.ifmo.splat.messages.Transaction;

/**
 * Created by Иван on 15.01.2017.
 */
public class TransactionMessage implements InnerMessage {
    private final Transaction transaction;

    public TransactionMessage(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
