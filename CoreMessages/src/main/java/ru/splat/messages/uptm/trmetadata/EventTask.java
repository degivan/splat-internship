package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;

import java.util.List;

/**
 * Created by Дмитрий on 17.12.2016.
 */
public class EventTask extends LocalTask {
    private final List<Long> selections;
    private final ServicesEnum service = ServicesEnum.EventService;

    public EventTask(TaskTypesEnum type,
                     List<Long> selections) {
        super(type);
        this.selections = selections;
    }


    public ServicesEnum getService() {
        return service;
    }

    public List<Long> getSelections() {
        return selections;
    }


}
