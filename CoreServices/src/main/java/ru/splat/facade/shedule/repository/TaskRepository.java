package ru.splat.facade.shedule.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;


public class TaskRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void deleteOldData(String tableName, long timeLimit)
    {
        String SQL_DELETE_DATA = "DELETE FROM " + tableName +" WHERE ? - record_timestamp > ?";
        long currentTime = System.currentTimeMillis();
        jdbcTemplate.update(SQL_DELETE_DATA, currentTime, timeLimit);
    }

}
