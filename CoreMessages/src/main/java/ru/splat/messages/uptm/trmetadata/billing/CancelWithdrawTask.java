package ru.splat.messages.uptm.trmetadata.billing;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 04.02.2017.
 */
//на данный момент набор данных для отмены первой фазы такой же, как и в самой первой фазе
public class CancelWithdrawTask extends LocalTask {
    private final Integer punterId;
    private final Integer sum;
    private final ServicesEnum service = ServicesEnum.BillingService;

    public CancelWithdrawTask(TaskTypesEnum type, Long time, Integer punterId, Integer sum) {
        super(type, time);
        this.punterId = punterId;
        this.sum = sum;
    }

    public Integer getPunterId() {
        return punterId;
    }

    public Integer getSum() {
        return sum;
    }

    @Override
    public ServicesEnum getService() {
        return null;
    }
}
