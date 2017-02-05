package ru.splat.messages.uptm.trmetadata.punter;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 04.02.2017.
 */
public class CancelPunterLimitsTask extends LocalTask {
    private final Integer punterId;
    private final ServicesEnum service = ServicesEnum.PunterService;
    public CancelPunterLimitsTask(Integer _punterId, Long time) {
        super(time);
        this.punterId = _punterId;
    }

    @Override
    public TaskTypesEnum getType() {
        return TaskTypesEnum.CANCEL_PUNTER_LIMITS;
    }

    @Override
    public ServicesEnum getService() {
        return service;
    }

    public Integer getPunterId() {
        return punterId;

    }
}
