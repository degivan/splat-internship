package ru.splat.Punter.feautures;


public class PunterProxy
{
    private long transactionId;
    private String services;

    public PunterProxy(long transactionId, String services)
    {
        this.transactionId = transactionId;
        this.services = services;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PunterProxy that = (PunterProxy) o;

        return transactionId == that.transactionId;

    }

    @Override
    public int hashCode() {
        return (int) (transactionId ^ (transactionId >>> 32));
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }
}
