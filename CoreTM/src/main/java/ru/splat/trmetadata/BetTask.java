package ru.splat.trmetadata;

import ru.splat.conventions.BetStatesEnum;
import ru.splat.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 22.12.2016.
 */
public class BetTask extends LocalTask {
    private final BetStatesEnum betState;
    private final Long punterId;
    private final String service = "BetService"; //это поле должно указывать на топик, с которого читает нужный сервис

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
    public String getService() {
        return service;
    }
}
