package ru.splat.Punter.feautures;


public class PunterBetTime
{

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
    private String services;

    public PunterBetTime(int id, long betTime, boolean checkLimit, long transactionId,String services)
    {
        this.id = id;
        this.betTime = betTime;
        this.checkLimit = checkLimit;
        this.transactionId = transactionId;
        this.services = services;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
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

    public void setCheckLimit(boolean checkLimit) {
        this.checkLimit = checkLimit;
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

}
