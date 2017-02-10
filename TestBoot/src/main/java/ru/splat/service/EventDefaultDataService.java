package ru.splat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.repository.EventRepository;

public class EventDefaultDataService
{
    public static final int EVENT_COUNT = 100;

    public static final int MARKET_COUNT = 100;

    private static final int OUTCOME_COUNT = 5;



    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public void insertDefaultData()
    {
        eventRepository.insertEvent(EVENT_COUNT);
        eventRepository.insertMarket(MARKET_COUNT*EVENT_COUNT);
        eventRepository.insertOutcome(MARKET_COUNT*OUTCOME_COUNT*EVENT_COUNT);
    }

    public boolean isEmptyEvent()
    {
       return (eventRepository.isExistEvent() == null || eventRepository.isExistEvent().isEmpty());
    }

    public void deleteData()
    {
        eventRepository.delete();
    }


}
