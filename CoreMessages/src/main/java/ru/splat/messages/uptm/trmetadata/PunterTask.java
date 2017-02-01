package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 17.12.2016.
 */
public class PunterTask extends LocalTask {
    private final Long punterId;
    private final ServicesEnum service = ServicesEnum.PunterService;
    public PunterTask(TaskTypesEnum type, Long _punterId) {
        super(type);
        this.punterId = _punterId;
    }

    @Override
    public ServicesEnum getService() {
        return service;
    }

    public Long getPunterId() {
        return punterId;

    }
}
