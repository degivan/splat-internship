package ru.splat.messages.uptm.trmetadata.event;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;

import java.util.Set;

/**
 * Created by Дмитрий on 04.02.2017.
 */
public class CancelSelectionLimitsTask extends LocalTask {
    private final Set<Integer> selections;
    private final ServicesEnum service = ServicesEnum.EventService;

    public CancelSelectionLimitsTask(TaskTypesEnum type, Long time,
                                  Set<Integer> selections) {
        super(type, time);
        this.selections = selections;
    }


    public ServicesEnum getService() {
        return service;
    }

    public Set<Integer> getSelections() {
        return selections;
    }
}