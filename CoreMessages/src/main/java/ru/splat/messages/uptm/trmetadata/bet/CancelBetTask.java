package ru.splat.messages.uptm.trmetadata.bet;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 04.02.2017.
 */
public class CancelBetTask extends LocalTask{
    private final Long betId;

    public Long getBetId() {
        return betId;
    }

    public CancelBetTask(TaskTypesEnum type, Long time, Long betId) {
        super(type, time);
        this.betId = betId;
    }

    @Override
    public ServicesEnum getService() {
        return null;
    }
}
