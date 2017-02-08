package ru.splat.messages.uptm.trstate;

import ru.splat.messages.conventions.ServicesEnum;

import java.util.Map;

/**
 * Created by Дмитрий on 22.12.2016.
 */
//ответ от TMFinalizer об выполненной транзакции - посылается registry
public class TransactionState {
    private final Long transactionId;
    //таски от всех сервисов
    private Map<ServicesEnum, ServiceResponse> localStates;
    private boolean isSent = false;

    public void setSent(boolean sent) {
        isSent = sent;
    }

    //конструктор вызывается при инициализации транзакции через TMActor или при получении сообщения от сервисов с информацией о тасках
    public TransactionState(Long transactionId, Map<ServicesEnum, ServiceResponse> localStates) {
        this.transactionId = transactionId;
        this.localStates = localStates;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setLocalState(ServicesEnum service, ServiceResponse state) {
        localStates.put(service, state);
    }

    public Map<ServicesEnum, ServiceResponse> getLocalStates() {
        return localStates;
    }
}
