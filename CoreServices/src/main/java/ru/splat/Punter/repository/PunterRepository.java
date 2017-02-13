package ru.splat.Punter.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.splat.facade.feautures.Limit;
import ru.splat.facade.repository.LimitRepository;
import ru.splat.facade.util.PunterUtil;
import ru.splat.Punter.feautures.PunterInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


public class PunterRepository implements LimitRepository
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int DEFAULT_LIMIT = 3;
    private static final int DEFAULT_LIMIT_TIME = 3 * 60 * 1000;

    @Override
    public List<Limit> getLimits(List<Integer> punterIdList)
    {
        if (punterIdList == null || punterIdList.isEmpty())
            return null;

        insertPunter(punterIdList);

        RowMapper<Limit> rm = (rs, rowNum) ->
        {
            Limit punterLimit = new Limit();
            punterLimit.setId(rs.getInt("id"));
            punterLimit.setLimit(rs.getInt("lim"));
            punterLimit.setLimitTime(rs.getInt("limit_time"));
            return punterLimit;
        };

        String SQL_SELECT_PUNTER_LIMITS = "SELECT id, lim, limit_time FROM punter WHERE ID IN (?)";
        return  jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdList.size(), SQL_SELECT_PUNTER_LIMITS), rm,
                punterIdList.toArray());
    }

    public void insertPunter(List<Integer> punterIdList)
    {
        if (punterIdList == null || punterIdList.isEmpty())
            return;

        String SQL_INSERT_PUNTER = "INSERT INTO punter (id,lim,types,limit_time) SELECT ?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM punter WHERE punter.id = ?)";
        jdbcTemplate.batchUpdate(SQL_INSERT_PUNTER, new BatchPreparedStatementSetter()
        {
            public void setValues(PreparedStatement ps, int i) throws SQLException
            {
                ps.setInt(1, punterIdList.get(i));
                ps.setLong(2, DEFAULT_LIMIT);
                ps.setNull(3, 1);
                ps.setLong(4, DEFAULT_LIMIT_TIME);
                ps.setInt(5, punterIdList.get(i));
            }

            public int getBatchSize() {
                return punterIdList.size();
            }
        });
    }

    @Override
    public void updateLimit(Limit limit)
    {
        String SQL_CANCEL_BET = "UPDATE punter SET lim = ?, limit_time = ? where id = ?";
        jdbcTemplate.update(SQL_CANCEL_BET,limit.getLimit(),limit.getLimitTime(),limit.getId());
    }


}