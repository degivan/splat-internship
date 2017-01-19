package ru.splat.trmetadata;

import ru.splat.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 22.12.2016.
 */
public class BillingTask extends LocalTask {
    private final Long punterId;
    private final Integer sum;
    private final String service = "BillingService"; //это поле должно указывать на топик, с которого читает нужный сервис

    public BillingTask(TaskTypesEnum type, Long _punterId, Integer sum) {
        super(type);
        this.punterId = _punterId;
        this.sum = sum;
    }
    public Integer getSum() {
        return sum;
    }

    @Override
    public String getService() {
        return service;
    }

    public Long getPunterId() {
        return punterId;
    }
}