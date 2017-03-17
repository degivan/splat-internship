package ru.splat.tm.messages;

/**
 * Created by Дмитрий on 17.03.2017.
 */
public class TMConsumerRecoverResponse {
    private final boolean successful;

    public TMConsumerRecoverResponse(boolean successful) {
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
