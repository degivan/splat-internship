package ru.splat.messages.uptm.trmetadata.billing;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 04.02.2017.
 */
//на данный момент набор данных для отмены первой фазы такой же, как и в самой первой фазе
public class CancelWithdrawTask extends LocalTask {
    private final Integer punterId;
    private final Integer sum;
    private final ServicesEnum service = ServicesEnum.BillingService;

    public CancelWithdrawTask(Integer punterId, Integer sum, Long time) {
        super(time);
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
    public TaskTypesEnum getType() {
        return TaskTypesEnum.CANCEL_RESERVE;
    }

    @Override
    public ServicesEnum getService() {
        return service;
    }

    public static CancelWithdrawTask create(BetInfo betInfo) {
        return new CancelWithdrawTask(betInfo.getUserId(),
                betInfo.getBet(),
                System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "CancelWithdrawTask{" +
                "punterId=" + punterId +
                ", sum=" + sum +
                ", service=" + service +
                "} ";
    }
}
