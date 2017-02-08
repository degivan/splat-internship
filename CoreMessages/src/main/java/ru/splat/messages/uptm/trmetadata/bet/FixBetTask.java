package ru.splat.messages.uptm.trmetadata.bet;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 04.02.2017.
 */
public class FixBetTask extends LocalTask {
    private final ServicesEnum service = ServicesEnum.BetService;
    private final Long betId;
    //конструктор второй фазы
    public FixBetTask(Long betId, Long time) {
        super(time);
        this.betId = betId;

    }

    public Long getBetId() {
        return betId;
    }

    @Override
    public TaskTypesEnum getType() {
        return TaskTypesEnum.FIX_BET;
    }

    @Override
    public ServicesEnum getService() {
        return service;
    }

    public static FixBetTask create(BetInfo betInfo) {
        return new FixBetTask(betInfo.getBetId(), System.currentTimeMillis());
    }
}
