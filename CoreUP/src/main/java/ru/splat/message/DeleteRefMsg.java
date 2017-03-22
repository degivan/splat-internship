package ru.splat.message;

import ru.splat.db.Bounds;
import ru.splat.messages.Transaction;

/**
 * After phaser completes his work.
 */
public class DeleteRefMsg {
    private final Bounds bounds;

    public DeleteRefMsg(Transaction transaction) {
        bounds = new Bounds(transaction.getLowerBound(), transaction.getUpperBound());
    }

    public Bounds getBounds() {
        return bounds;
    }
}
