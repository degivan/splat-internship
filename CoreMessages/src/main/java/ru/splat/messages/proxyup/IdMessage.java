package ru.splat.messages.proxyup;

/**
 * ProxyUPMessage which contains transaction identifier.
 */
public class IdMessage extends ProxyUPMessage {
    private final Long transactionId;

    public IdMessage(Long transactionId, Integer userId) {
        super(userId);
        this.transactionId = transactionId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    @Override
    public String toString() {
        return "IdMessage{" +
                "transactionId=" + transactionId +
                '}';
    }


}
