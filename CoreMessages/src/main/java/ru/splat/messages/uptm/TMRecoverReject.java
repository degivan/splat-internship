package ru.splat.messages.uptm;

/**
 * Created by Дмитрий on 16.03.2017.
 */
public class TMRecoverReject {
    private final String reason;

    public TMRecoverReject(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
