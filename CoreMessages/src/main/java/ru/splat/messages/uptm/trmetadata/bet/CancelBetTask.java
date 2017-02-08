package ru.splat.messages.uptm.trmetadata.bet;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 04.02.2017.
 */
public class CancelBetTask extends LocalTask {
    private final Long betId;

    public Long getBetId() {
        return betId;
    }

    public CancelBetTask(Long betId, Long time) {
        super(time);
        this.betId = betId;
    }

    @Override
    public TaskTypesEnum getType() {
        return TaskTypesEnum.CANCEL_BET;
    }

    @Override
    public ServicesEnum getService() {
        return ServicesEnum.BetService;
    }

    public static CancelBetTask create(BetInfo betInfo) {
        return new CancelBetTask(betInfo.getBetId(), System.currentTimeMillis());
    }
}
