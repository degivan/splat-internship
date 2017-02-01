package ru.splat.messages.uptm;

/**
 * Response from TM to UP after request processing.
 */
public class TMResponse {
    private final Long transactionId;

    public TMResponse(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

}
