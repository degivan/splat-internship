package ru.splat.Billing.feautures;


import ru.splat.protobuf.PunterRes;

public class TransactionResult {

    public TransactionResult() {
    }

    public TransactionResult(long transactionId, PunterRes.Punter punter) {
        this.transactionId = transactionId;
        this.punter = punter;
    }

    private long transactionId;
    private PunterRes.Punter punter;

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setPunter(PunterRes.Punter punter) {
        this.punter = punter;
    }

    public long getTransactionId() {

        return transactionId;
    }

    public PunterRes.Punter getPunter() {
        return punter;
    }
}
