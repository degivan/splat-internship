package ru.splat.messages.uptm;

import ru.splat.messages.conventions.LocalStatesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;

import java.util.Map;

/**
 * Created by Дмитрий on 22.12.2016.
 */
//ответ от TMFinalizer об выполненной транзакции - посылается registry
public class TransactionState {
    private final Long transactionId;
    private Map<TaskTypesEnum, LocalStatesEnum> localStates;

    public TransactionState(Long transactionId, Map<TaskTypesEnum, LocalStatesEnum> localStates) {
        this.transactionId = transactionId;
        this.localStates = localStates;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setLocalState(TaskTypesEnum task, LocalStatesEnum state) {
        localStates.put(task, state);
    }

    public Map<TaskTypesEnum, LocalStatesEnum> getLocalStates() {
        return localStates;
    }
}
