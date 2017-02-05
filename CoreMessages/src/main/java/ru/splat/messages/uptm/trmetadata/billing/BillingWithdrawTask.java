package ru.splat.messages.uptm.trmetadata.billing;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 22.12.2016.
 */
public class BillingWithdrawTask extends LocalTask {
    private final Integer punterId;
    private final Integer sum;
    private final ServicesEnum service = ServicesEnum.BillingService;

    public BillingWithdrawTask(TaskTypesEnum type, Long time, Integer _punterId, Integer sum) {
        super(type, time);
        this.punterId = _punterId;
        this.sum = sum;
    }
    public Integer getSum() {
        return sum;
    }

    @Override
    public ServicesEnum getService() {
        return service;
    }

    public Integer getPunterId() {
        return punterId;
    }
}