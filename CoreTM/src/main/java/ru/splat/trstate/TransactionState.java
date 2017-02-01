package ru.splat.trstate;

import ru.splat.conventions.LocalStatesEnum;
import ru.splat.conventions.TaskTypesEnum;

import java.util.Map;

/**
 * Created by Дмитрий on 22.12.2016.
 */
public class TransactionState {
    final Long transactionId;
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
