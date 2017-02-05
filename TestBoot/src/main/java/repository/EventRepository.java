package repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventRepository
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final double CURRENT_KOEF = 1.5;
    private static final String STATUS = "NOT DESIGNED";
    private static final int LIMIT = 3;
    private static final long LIMIT_TIME = 180000;

    public void insertDefaultData()
    {

    }

    public void insertEvent(Integer eventCount)
    {
        String SQL_INSERT_EVENT= "INSERT INTO event (id,name,status) VALUES (?,?, 'NOT DESIGNED')";
        jdbcTemplate.batchUpdate(SQL_INSERT_EVENT, new BatchPreparedStatementSetter()
        {
            public void setValues(PreparedStatement ps, int i) throws SQLException
            {
                ps.setInt(1, i);
                ps.setString(2, i + " ");
            }

            public int getBatchSize() {
                return eventCount;
            }
        });
    }

    public void insertMarket(int marketCount)
    {
        String SQL_INSERT_MARKET= "INSERT INTO market (id,name,event_id) VALUES (?,?,?)";
        jdbcTemplate.batchUpdate(SQL_INSERT_MARKET, new BatchPreparedStatementSetter()
        {
            public void setValues(PreparedStatement ps, int i) throws SQLException
            {
                ps.setInt(1, i);
                ps.setString(2, i + " ");
                ps.setInt(3,i/100);
            }

            public int getBatchSize() {
                return marketCount;
            }
        });
    }

    public void insertOutcome(int outcomeCount)
    {
        String SQL_INSERT_OUTCOME = "INSERT INTO outcome (id,name,current_koef, status, market_id, lim, limit_time) VALUES (?,?,?,CAST (? AS status),?,?,?)";
        jdbcTemplate.batchUpdate(SQL_INSERT_OUTCOME, new BatchPreparedStatementSetter()
        {
            public void setValues(PreparedStatement ps, int i) throws SQLException
            {
                ps.setInt(1, i);
                ps.setString(2, i + " ");
                ps.setDouble(3,CURRENT_KOEF);
                ps.setString(4, STATUS);
                ps.setInt(5,i/5);
                ps.setInt(6,LIMIT );
                ps.setLong(7, LIMIT_TIME);
            }

            public int getBatchSize() {
                return outcomeCount;
            }
        });
    }
}
