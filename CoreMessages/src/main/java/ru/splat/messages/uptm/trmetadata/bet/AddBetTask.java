package ru.splat.messages.uptm.trmetadata.bet;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Дмитрий on 22.12.2016.
 */
//первая фаза по ставкам
public class AddBetTask extends LocalTask {
    private final Integer punterId;
    private final ServicesEnum service = ServicesEnum.BetService;
    private final Set<BetOutcome> betOutcomes; //список возможных исходов

    public Integer getPunterId() {
        return punterId;
    }

    public Set<BetOutcome> getBetOutcomes() {
        return betOutcomes;
    }
    //конструктор первой фазы
    public AddBetTask(TaskTypesEnum type, Long time, Integer punterId, Set<BetOutcome> betOutcomes) {
        super(type, time);
        this.punterId = punterId;
        this.betOutcomes = betOutcomes;
    }





    @Override
    public ServicesEnum getService() {
        return service;
    }


}
