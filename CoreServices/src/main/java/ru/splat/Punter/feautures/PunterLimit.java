package ru.splat.Punter.feautures;


public class PunterLimit
{

    /**
     * ID игрока
     */
    private int id;

    /**
     * Лимит игрока
     */
    private int limit;

    /**
     * Время действия лимита в миллисекундах
     */
    private int timeLimit;

    /**
     * ID транзакци
     */
    private long transactionId;

    public PunterLimit()
    {
        id = 0;
        limit = 0;
        timeLimit = 0;
        transactionId = 0;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public int getId() {
        return id;
    }

    public int getLimit() {
        return limit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
}
