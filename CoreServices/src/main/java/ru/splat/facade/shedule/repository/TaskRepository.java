package ru.splat.facade.shedule.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;


public class TaskRepository
{

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional
    public void deleteOldData(String tableName, long timeLimit)
    {
        String SQL_DELETE_DATA = "DELETE FROM " + tableName +" WHERE ? - record_timestamp > ?";
        long currentTime = System.currentTimeMillis();
        jdbcTemplate.update(SQL_DELETE_DATA, currentTime, timeLimit);
    }

}
