package ru.splat.message;

import ru.splat.db.Bounds;

/**
 * Created by Иван on 01.02.2017.
 */
public class NewIdsMessage implements InnerMessage {
    private final Bounds bounds;

    public NewIdsMessage(Bounds bounds) {
        this.bounds = bounds;
    }

    public Bounds getBounds() {
        return bounds;
    }
}
