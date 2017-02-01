package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.conventions.BetStatesEnum;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 22.12.2016.
 */
public class BetTask extends LocalTask {
    private final BetStatesEnum betState;
    private final Long punterId;
    private final ServicesEnum service = ServicesEnum.BetService;

    public Long getPunterId() {
        return punterId;
    }

    public BetStatesEnum getBetState() {
        return betState;
    }

    public BetTask(TaskTypesEnum type, BetStatesEnum betState, Long punterId) {
        super(type);
        this.betState = betState;
        this.punterId = punterId;
    }

    @Override
    public ServicesEnum getService() {
        return service;
    }
}
