package ru.splat.messages.uptm;

/**
 * Confirmation that TransactionMetadata message was delivered.
 * отправляется TMActor-м сразу после отправки данных в кафку
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
