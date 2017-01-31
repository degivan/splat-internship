package ru.ifmo.splat.messages;

import ru.ifmo.splat.messages.proxyup.bet.BetInfo;

/**
 * Wrapper class for Transaction.
 */
public class Transaction {
    //TODO: change to range of identifiers
    private Long transactionId;
    private State state;
    private BetInfo betInfo;

    public static Transaction statelessTransaction(BetInfo requestInfo) {
        Transaction transaction = new Transaction();
        transaction.setBetInfo(requestInfo);
        return transaction;
    }

    public BetInfo getBetInfo() {
        return betInfo;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public State getState() {
        return state;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setBetInfo(BetInfo betInfo) {
        this.betInfo = betInfo;
    }

    public enum State {
        CREATED, CANCEL, COMPLETED, DENIED, PHASE1_RESPONDED, PHASE2_SEND, CANCEL_COMPLETED
    }
}
