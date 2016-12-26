package ru.splat.Shedule;

import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.PunterService;
import ru.splat.repository.PunterRepository;


public class SheduleCleaningDB {

    private static final long PERIOD_TIME = 10 * 60 * 1000;

    private String tableName;
    private long timeLimit;

    @Autowired
    PunterRepository punterRepository;

    public void cleanTable() { punterRepository.deleteOldData(tableName, timeLimit);}

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
