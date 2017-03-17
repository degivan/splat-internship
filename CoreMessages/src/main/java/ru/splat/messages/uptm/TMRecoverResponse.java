package ru.splat.messages.uptm;

/**
 * Created by Иван on 23.02.2017.
 */
public class TMRecoverResponse {
    private final boolean successful;
    private final String reason;

    private TMRecoverResponse(boolean successful, String reason) {
        this.successful = successful;
        this.reason = reason;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getReason() {
        return reason;
    }

    public static TMRecoverResponse confirmTMRecover() {
        return new TMRecoverResponse(true, "TMRecover success");
    }

    public static TMRecoverResponse rejectTMRecover(String reason) {
        return new TMRecoverResponse(false, reason);
    }

}
