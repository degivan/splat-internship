package ru.splat.messages.uptm.trmetadata.event;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.uptm.trmetadata.LocalTask;

import java.util.Set;

/**
 * Created by Дмитрий on 04.02.2017.
 */
public class CancelSelectionLimitsTask extends LocalTask {
    private final Set<Integer> selections;
    private final ServicesEnum service = ServicesEnum.EventService;

    public CancelSelectionLimitsTask(
            Set<Integer> selections, Long time) {
        super(time);
        this.selections = selections;
    }


    @Override
    public TaskTypesEnum getType() {
        return TaskTypesEnum.CANCEL_SELECTION_LIMIT;
    }

    public ServicesEnum getService() {
        return service;
    }

    public Set<Integer> getSelections() {
        return selections;
    }

    public static CancelSelectionLimitsTask create(BetInfo betInfo) {
        return new CancelSelectionLimitsTask(betInfo.getSelectionsId(), System.currentTimeMillis());
    }
}
