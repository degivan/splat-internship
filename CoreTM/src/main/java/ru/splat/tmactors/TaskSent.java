package ru.splat.tmactors;

import ru.splat.messages.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 06.02.2017.
 */
public class TaskSent {
    private final Long transactionId;
    private final TaskTypesEnum taskType;

    public TaskSent(Long transactionId, TaskTypesEnum taskType) {
        this.transactionId = transactionId;
        this.taskType = taskType;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public TaskTypesEnum getTaskType() {
        return taskType;
    }
}
