package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 11.12.2016.
 */
//инкапсулирует одну из локальных операций транзакции
public abstract class LocalTask {
    private final Long time;

    public LocalTask(Long time) {
        this.time = time;
    }

    public abstract TaskTypesEnum getType();

    public abstract ServicesEnum getService();

    public Long getTime() {
        return time;
    }
}