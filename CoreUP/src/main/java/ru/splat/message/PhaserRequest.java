package ru.splat.message;

import ru.ifmo.splat.messages.Transaction;

/**
 * Created by Иван on 15.01.2017.
 */
public class PhaserRequest extends TransactionMessage {
    public PhaserRequest(Transaction transaction) {
        super(transaction);
    }
}
