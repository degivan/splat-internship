package ru.splat.trmetadata;

import ru.splat.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 17.12.2016.
 */
public class PunterTask extends LocalTask {
    private final Long punterId;
    private final String service = "PunterService"; //это поле должно указывать на топик, с которого читает нужный сервис
    public PunterTask(TaskTypesEnum type, Long _punterId) {
        super(type);
        this.punterId = _punterId;
    }

    @Override
    public String getService() {
        return service;
    }

    public Long getPunterId() {
        return punterId;

    }
}
