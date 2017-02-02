package ru.splat.Punter.feautures;

import ru.splat.facade.feautures.TransactionRequest;

import java.util.List;

public class PunterInfo implements TransactionRequest
{

    private int localTask;
    private int punterId;
    private long transactionId;
    List<Integer> services;
    private long time;

    public PunterInfo()
    {
    }

    public PunterInfo(int punterId, long transactionId, int localTask,List<Integer> services, long time)
    {
        this.punterId = punterId;
        this.transactionId = transactionId;
        this.localTask = localTask;
        this.services = services;
        this.time = time;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PunterInfo punterInfo = (PunterInfo) o;

        return transactionId == punterInfo.transactionId;

    }

    @Override
    public int hashCode() {
        return (int) (transactionId ^ (transactionId >>> 32));
    }

    @Override
    public String toString()
    {
        return "PunterInfo{" +
                "localTask=" + localTask +
                ", punterId=" + punterId +
                ", transactionId=" + transactionId +
                ", services='" + services + '\'' +
                ", time=" + time +
                '}';
    }

    @Override
    public List<Integer> getServices() {
        return services;
    }

    public void setServices(List<Integer> services) {
        this.services = services;
    }

    public int getId() {return punterId;}

    public long getTransactionId() {
        return transactionId;
    }

    public void setPunterId(int punterId) {
        this.punterId = punterId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public int getLocalTask() {
        return localTask;
    }

    public void setLocalTask(int localTask) {
        this.localTask = localTask;
    }

    public long getTime() { return time;}

    public void setTime(long time) {this.time = time;}
}