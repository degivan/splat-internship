package ru.splat.message;

import ru.splat.messages.Transaction;

import java.util.List;

/**
 * Message for receiver asking to continue work on transaction.
 */
public class RecoverRequest implements InnerMessage {
    private final List<Transaction> transactions;

    public RecoverRequest(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return "RecoverRequest{" +
                "transactions=" + transactions.size() +
                '}';
    }
}
