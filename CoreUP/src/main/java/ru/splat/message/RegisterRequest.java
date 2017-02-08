package ru.splat.message;

import akka.actor.ActorRef;

/**
 * Message from receiver to registry.
 */
public class RegisterRequest implements InnerMessage {
    private final Long transactionId;
    private final ActorRef actor;

    public RegisterRequest(Long trId, ActorRef actor) {
        this.transactionId = trId;
        this.actor = actor;
    }

    public ActorRef getActor() {
        return actor;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "transactionId=" + transactionId +
                ", actor=" + actor +
                '}';
    }
}
