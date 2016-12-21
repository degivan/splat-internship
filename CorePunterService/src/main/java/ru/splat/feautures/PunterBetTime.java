package ru.splat.feautures;

/**
 * Created by Rustam on 12/17/2016.
 */
public class PunterBetTime {

    /**
     * ID игрока
     */
    private int id;

    /**
     * Время ставки
     */
    private long betTime;

    /**
     * Результат проверки лимита
     */
    private boolean checkLimit;

    /**
     * ID транзакции
     */
    private long transactionId;

    public PunterBetTime(int id, long betTime, boolean checkLimit, long transactionId) {
        this.id = id;
        this.betTime = betTime;
        this.checkLimit = checkLimit;
        this.transactionId = transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBetTime(long betTime) {
        this.betTime = betTime;
    }

    public int getId() {
        return id;
    }

    public long getBetTime() {
        return betTime;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public boolean isCheckLimit() {
        return checkLimit;
    }

    public void setCheckLimit(boolean checkLimit) {
        this.checkLimit = checkLimit;
    }
}
