package ru.splat.facade.shedule;

import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.Punter.facade.repository.PunterRepository;
import ru.splat.facade.shedule.repository.TaskRepository;

import static org.apache.log4j.LogManager.getLogger;


public class SheduleCleaningDB
{


    private static final long PERIOD_TIME = 10 * 60 * 1000;

    private String tableName;
    private long timeLimit;

    @Autowired
    TaskRepository punterRepository;

    public void cleanTable()
    {
        punterRepository.deleteOldData(tableName,  timeLimit);
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
