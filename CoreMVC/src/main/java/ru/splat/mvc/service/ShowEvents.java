package ru.splat.mvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.splat.mvc.features.EventInfo;
import ru.splat.mvc.features.MarketInfo;
import ru.splat.mvc.features.OutcomeInfo;
import ru.splat.mvc.features.ReposResult;
import ru.splat.mvc.repository.EventRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShowEvents
{

    EventRepository eventRepository = new EventRepository();

    public ReposResult initMainPage()
    {
        List<EventInfo> eventInfoList = eventRepository.getEvents();
        Map<Integer,List<MarketInfo>> eventMap = eventInfoList.stream().collect(Collectors.toMap((entry) -> entry.getId(),(entry) -> eventRepository.getMarkets(entry.getId())));
        Map<Integer,List<OutcomeInfo>> marketMap = new HashMap<>();
        for (List<MarketInfo> marketInfos : eventMap.values())
        {
            for (MarketInfo marketInfo:marketInfos)
            {
                marketMap.put(marketInfo.getId(),eventRepository.getOutcomes(marketInfo.getId()));
            }
        }
        return new ReposResult(eventMap,marketMap,eventInfoList);
    }

}
