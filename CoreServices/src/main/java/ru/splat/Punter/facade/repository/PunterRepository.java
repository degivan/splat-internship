package ru.splat.Punter.facade.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.facade.util.PunterUtil;
import ru.splat.Punter.feautures.PunterInfo;
import ru.splat.Punter.feautures.PunterBetTime;
import ru.splat.Punter.feautures.PunterLimit;
import ru.splat.facade.feautures.RepAnswer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class PunterRepository
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int DEFAULT_LIMIT = 3;
    private static final int DEFAULT_LIMIT_TIME = 60 * 60 * 1000;

    /**
     * @param punterLimit макс кол-во ставок игрока за единицу времени
     * @return список времен последних ставок
     */
    private List<PunterBetTime> getPunterBetTimes(PunterLimit punterLimit)
    {
        String SQL_SELECT_PUNTER_BET_TIME = "SELECT id, record_timestamp FROM punter_timestamps WHERE id = ? ORDER BY record_timestamp DESC LIMIT ?";
        RowMapper<PunterBetTime> rm = (rs, rowNum) ->
                new PunterBetTime(rs.getInt("id"), rs.getLong("record_timestamp"), true, punterLimit.getTimeLimit());
        return jdbcTemplate.query(SQL_SELECT_PUNTER_BET_TIME, rm, punterLimit.getId(), punterLimit.getLimit());
    }

    private List<PunterBetTime> puntersArbiter(List<PunterLimit> punterLimits)
    {
        if (punterLimits == null || punterLimits.isEmpty())
            return null;

        List<PunterBetTime> result = new ArrayList<>();
        for (PunterLimit punterLimit : punterLimits)
        {
            List<PunterBetTime> punterBetTimes = getPunterBetTimes(punterLimit);

            long currentTime = System.currentTimeMillis();
            boolean check = punterBetTimes == null || punterBetTimes.isEmpty() || punterBetTimes.size() < punterLimit.getLimit() ||
                    currentTime - punterBetTimes.get(punterBetTimes.size() - 1).getBetTime() > punterLimit.getTimeLimit();

            result.add(new PunterBetTime(punterLimit.getId(), currentTime, check, punterLimit.getTransactionId()));
        }
        return result;
    }

    private List<PunterLimit> getPunterLimits(Map<Integer, Long> punterIdMap)
    {
        if (punterIdMap == null || punterIdMap.isEmpty())
            return null;
        RowMapper<PunterLimit> rm = (rs, rowNum) ->
        {
            PunterLimit punterLimit = new PunterLimit();
            punterLimit.setId(rs.getInt("id"));
            punterLimit.setLimit(rs.getInt("lim"));
            punterLimit.setTimeLimit(rs.getInt("limit_time"));
            punterLimit.setTransactionId(punterIdMap.get(punterLimit.getId()));
            return punterLimit;
        };

        String SQL_SELECT_PUNTER_LIMITS = "SELECT id, lim, limit_time FROM punter WHERE ID IN (?)";
        return  jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdMap.size(), SQL_SELECT_PUNTER_LIMITS), rm,
                punterIdMap.keySet().toArray());
    }

    public void deleteBetTimes(List<PunterInfo> punterIdList)
    {
        if (punterIdList == null || punterIdList.isEmpty())
            return;

        String SQL_DELETE_PUNTER_TIMESTAMP = "DELETE FROM punter_timestamps WHERE transaction_id = ?";
        jdbcTemplate.batchUpdate(SQL_DELETE_PUNTER_TIMESTAMP, new BatchPreparedStatementSetter()
        {

            public void setValues(PreparedStatement ps, int i) throws SQLException
            {
                PunterInfo punterPunterInfo = punterIdList.get(i);
                ps.setLong(1, punterPunterInfo.getTransactionId());
            }

            public int getBatchSize() {
                return punterIdList.size();
            }
        });
    }

    public void insertBatch(List<PunterBetTime> punterBetTimes)
    {
        if (punterBetTimes == null || punterBetTimes.isEmpty())
            return;

        String SQL_INSERT_PUNTER_TIMESTAMP = "INSERT INTO punter_timestamps (id, record_timestamp, transaction_id) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(SQL_INSERT_PUNTER_TIMESTAMP, new BatchPreparedStatementSetter()
        {

            public void setValues(PreparedStatement ps, int i) throws SQLException
            {
                PunterBetTime punterBetTime = punterBetTimes.get(i);
                ps.setInt(1, punterBetTime.getId());
                ps.setLong(2, punterBetTime.getBetTime());
                ps.setLong(3, punterBetTime.getTransactionId());
            }

            public int getBatchSize() {
                return punterBetTimes.size();
            }
        });
    }

    private void insertPunter(List<PunterInfo> punterIdList) {
        if (punterIdList == null || punterIdList.isEmpty())
            return;

        String SQL_INSERT_PUNTER = "INSERT INTO punter (id,lim,types,limit_time) SELECT ?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM punter WHERE punter.id = ?)";
        jdbcTemplate.batchUpdate(SQL_INSERT_PUNTER, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, punterIdList.get(i).getPunterId());
                ps.setLong(2, DEFAULT_LIMIT);
                ps.setNull(3, 1);
                ps.setLong(4, DEFAULT_LIMIT_TIME);
                ps.setInt(5, punterIdList.get(i).getPunterId());
            }

            public int getBatchSize() {
                return punterIdList.size();
            }
        });
    }

    public Set<RepAnswer> phase1(Set<PunterInfo> punterIdSet)
    {
        if (punterIdSet == null || punterIdSet.isEmpty())
            return null;
        Map<Integer, Long> punterIdMap = new HashMap<>();
        List<PunterInfo> punterIdList = new ArrayList<>();
        punterIdSet.parallelStream().forEach(i ->
        {
            punterIdMap.put(i.getPunterId(), i.getTransactionId());
            punterIdList.add(i);
        });

        insertPunter(punterIdList);

        List<PunterLimit> punterLimits = getPunterLimits(punterIdMap);
        List<PunterBetTime> result = puntersArbiter(punterLimits);

        insertBatch(result.stream().filter(PunterBetTime::isCheckLimit).collect(Collectors.toList()));
        return result.stream().map((map) -> new RepAnswer(map.getTransactionId(), map.isCheckLimit(),"Sucessefull")).collect(Collectors.toSet());
    }

    public Set<RepAnswer> cancel(List<PunterInfo> timestamps)
    {
        deleteBetTimes(timestamps);
        return new HashSet<RepAnswer>(timestamps.stream().map((map) -> new RepAnswer(map.getTransactionId(), true, "Sucessefull")).collect(Collectors.toSet()));
    }

}