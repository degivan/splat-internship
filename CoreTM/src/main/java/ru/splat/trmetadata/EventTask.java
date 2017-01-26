package ru.splat.trmetadata;

import ru.splat.conventions.TaskTypesEnum;

import java.util.List;

/**
 * Created by Дмитрий on 17.12.2016.
 */
public class EventTask extends LocalTask {
    private final List<Long> eventIdList;
    private final List<Long> selectionIdList;
    private final Long punterId;
    private final String service = "EventService"; //это поле должно указывать на топик, с которого читает нужный сервис

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

    public String getService() {
        return service;
    }

    public List<Long> getSelectionIdList() {

        return selectionIdList;
    }


}
