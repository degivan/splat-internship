package ru.splat.message;

/**
 * Created by Иван on 23.02.2017.
 */
public class RecoverResponse implements InnerMessage {
    private final boolean positive;

    public RecoverResponse(boolean positive) {
        this.positive = positive;
    }

    public boolean isPositive() {
        return positive;
    }
}
