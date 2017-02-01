package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;

import java.util.List;

/**
 * Created by Дмитрий on 22.12.2016.
 */
public class BetTask extends LocalTask {
    private final Long punterId;
    private final ServicesEnum service = ServicesEnum.BetService;
    private final List<BetOutcome> betOutcomes; //список возможных исходов



    public Long getPunterId() {
        return punterId;
    }

    public List getBetOutcomes() {
        return betOutcomes;
    }

    public BetTask(TaskTypesEnum type, Long punterId, List<BetOutcome> betOutcomes) {
        super(type);
        this.punterId = punterId;
        this.betOutcomes = betOutcomes;
    }

    @Override
    public ServicesEnum getService() {
        return service;
    }


}
