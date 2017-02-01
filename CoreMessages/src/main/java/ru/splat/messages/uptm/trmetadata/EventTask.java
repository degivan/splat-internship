package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;

import java.util.List;

/**
 * Created by Дмитрий on 17.12.2016.
 */
public class EventTask extends LocalTask {
    private final List<Long> eventIdList;
    private final List<Long> selectionIdList;
    private final Long punterId;
    private final ServicesEnum service = ServicesEnum.EventService;

    public EventTask(TaskTypesEnum type,
                     List<Long> eventIdList, List<Long> selectionIdList, Long punterId) {
        super(type);
        this.eventIdList = eventIdList;
        this.selectionIdList = selectionIdList;
        this.punterId = punterId;
    }

    public List<Long> getEventIdList() {
        return eventIdList;
    }


    public Long getPunterId() {
        return punterId;
    }

    public ServicesEnum getService() {
        return service;
    }

    public List<Long> getSelectionIdList() {
        return selectionIdList;
    }


}
