package ru.splat.trmetadata;

import ru.splat.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 11.12.2016.
 */
//инкапсулирует одну из локальных операций транзакции
public abstract class LocalTask {
    private final TaskTypesEnum type; //тип локальной транзакции
    public LocalTask(TaskTypesEnum type) {
        this.type = type;
    }

    public TaskTypesEnum getType() {
        return type;
    }
    public abstract String getService();



}