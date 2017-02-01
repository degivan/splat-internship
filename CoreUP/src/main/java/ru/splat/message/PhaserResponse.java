package ru.splat.message;

import ru.splat.messages.Transaction;

/**
 * Created by Иван on 15.01.2017.
 */
public class PhaserResponse extends TransactionMessage {
    public PhaserResponse(Transaction transaction) {
        super(transaction);
    }
}
