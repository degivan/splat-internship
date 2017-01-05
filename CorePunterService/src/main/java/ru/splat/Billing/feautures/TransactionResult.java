package ru.splat.Billing.feautures;


import com.google.protobuf.Message;
import ru.splat.protobuf.PunterRes;

// это плохой транзакшн резалт
// хороший транзакшн резалт содержит в себе пару {trId, message},
// где message -- обговорённый с сервисом ответ в кафку
public class TransactionResult {

    public TransactionResult() {
    }

    public TransactionResult(long transactionId, PunterRes.Punter punter) {
        this.transactionId = transactionId;
        this.punter = punter;
        this.result = null;
    }

    private long transactionId;
    private PunterRes.Punter punter;
    private Message result;

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setPunter(PunterRes.Punter punter) {
        this.punter = punter;
    }

    public long getTransactionId() {

        return transactionId;
    }


    public Message getResult()
    {
        return result;
    }


    public PunterRes.Punter getPunter() {
        return punter;
    }
}
