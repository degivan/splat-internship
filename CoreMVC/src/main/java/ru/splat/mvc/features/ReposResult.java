package ru.splat.mvc.features;


import java.util.List;
import java.util.Map;

public class ReposResult
{

    private Map<Integer,List<MarketInfo>> eventMap;
    private Map<Integer,List<OutcomeInfo>> marketMap;
    private List<EventInfo> eventInfoList;

    public ReposResult(Map<Integer, List<MarketInfo>> eventMap, Map<Integer, List<OutcomeInfo>> marketMap, List<EventInfo> eventInfoList)
    {
        this.eventMap = eventMap;
        this.marketMap = marketMap;
        this.eventInfoList = eventInfoList;
    }

    public Map<Integer, List<MarketInfo>> getEventMap() {return eventMap;}

    public Map<Integer, List<OutcomeInfo>> getMarketMap()
    {
        return marketMap;
    }
    public List<EventInfo> getEventInfoList()
    {
        return eventInfoList;
    }
}
