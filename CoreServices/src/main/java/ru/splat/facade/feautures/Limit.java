package ru.splat.facade.feautures;

public class Limit
{
    private int id;
    private int limit;
    private int limitTime;

    public Limit() {}

    public Limit(int id, int limit, int limitTime)
    {
        this.id = id;
        this.limit = limit;
        this.limitTime = limitTime;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Limit limit1 = (Limit) o;

        if (id != limit1.id) return false;
        if (limit != limit1.limit) return false;
        return limitTime == limit1.limitTime;

    }

    public int getId() {return id;}

    public void setId(int id) {
        this.id = id;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }
}
