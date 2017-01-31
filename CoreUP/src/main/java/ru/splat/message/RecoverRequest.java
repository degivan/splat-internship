package ru.splat.message;

import ru.ifmo.splat.messages.Transaction;

/**
 * Message for receiver asking to continue work on transaction.
 */
public class RecoverRequest extends TransactionMessage {
    public RecoverRequest(Transaction transaction) {
        super(transaction);
    }
}
