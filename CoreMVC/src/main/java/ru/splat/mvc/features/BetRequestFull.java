package ru.splat.mvc.features;

import java.util.List;
import java.util.Map;

public class BetRequestFull
{
    private Map<Integer,BetRequest> betInfo;
    private int sum;
    private int userId;

    public BetRequestFull()
    {
    }

    public BetRequestFull(Map<Integer, BetRequest> betInfo, int sum, int userId)
    {
        this.betInfo = betInfo;
        this.sum = sum;
        this.userId = userId;
    }

    public Map<Integer, BetRequest> getBetInfo() {
        return betInfo;
    }

    public void setBetInfo(Map<Integer, BetRequest> betInfo) {
        this.betInfo = betInfo;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
