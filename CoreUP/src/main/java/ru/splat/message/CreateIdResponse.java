package ru.splat.message;

import ru.splat.messages.Transaction;

/**
 * Response from id_generator to receiver.
 */
public class CreateIdResponse extends TransactionMessage {

    public CreateIdResponse(Transaction transaction) {
        super(transaction);
    }
}
