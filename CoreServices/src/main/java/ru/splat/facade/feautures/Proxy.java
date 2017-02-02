package ru.splat.facade.feautures;


import java.util.Deque;

public class Proxy
{
    private int limit;
    private long limitTime;
    private Deque<Long> deque;

    public Proxy(int limit, long limitTime, Deque<Long> deque)
    {
        this.limit = limit;
        this.limitTime = limitTime;
        this.deque = deque;
    }

    public Deque<Long> getDeque() {
        return deque;
    }

    public void setDeque(Deque<Long> deque) {
        this.deque = deque;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(long limitTime) {
        this.limitTime = limitTime;
    }
}
