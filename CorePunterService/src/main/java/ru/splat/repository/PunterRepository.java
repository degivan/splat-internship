package ru.splat.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.splat.PunterUtil;
import ru.splat.feautures.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Repository
public class PunterRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int DEFAULT_LIMIT = 20;
    private static final int DEFAULT_LIMIT_TIME = 60 * 60 * 1000;

    private static final String SQL_INSERT_PUNTER = "INSERT INTO punter (id,lim,types,limit_time) SELECT ?,?,?,? " +
            "WHERE NOT EXISTS (SELECT 1 FROM punter WHERE punter.id = ?)";
    private static final String SQL_SELECT_PUNTER_LIMITS = "SELECT id, lim, limit_time FROM punter WHERE ID IN (?)";
    private static final String SQL_SELECT_PUNTER_BET_TIME = "SELECT id, punter_timestamp FROM punter_timestamps " +
            "WHERE id = ? ORDER BY punter_timestamp DESC LIMIT ?";
    private static final String SQL_INSERT_PUNTER_IDEMP = "INSERT INTO punter_idemp (transaction_id, result, punter_timestamp) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_PUNTER_TIMESTAMP = "INSERT INTO punter_timestamps " +
            "(id, punter_timestamp, transaction_id) VALUES (?, ?, ?)";
    private static final String SQL_DELETE_PUNTER_TIMESTAMP = "DELETE FROM punter_timestamps WHERE transaction_id = ?";
    private static final String SQL_CHECK_IDEMPOTY = "SELECT transaction_id, result FROM PUNTER_IDEMP WHERE transaction_id IN (?)";
    /**
     * @param punterLimit макс кол-во ставок игрока за единицу времени
     * @return список времен последних ставок
     */
    private List<PunterBetTime> getPunterBetTimes(PunterLimit punterLimit) {
        RowMapper<PunterBetTime> rm = (rs, rowNum) ->
                new PunterBetTime(rs.getInt("id"), rs.getLong("punter_timestamp"), true, punterLimit.getTimeLimit());
        return jdbcTemplate.query(SQL_SELECT_PUNTER_BET_TIME, rm, punterLimit.getId(), punterLimit.getLimit());
    }

    private List<PunterBetTime> puntersArbiter(List<PunterLimit> punterLimits) {
        if (punterLimits == null || punterLimits.isEmpty())
            return null;

        List<PunterBetTime> result = new ArrayList<>();
        for (PunterLimit punterLimit : punterLimits) {
            List<PunterBetTime> punterBetTimes = getPunterBetTimes(punterLimit);

            long currentTime = System.currentTimeMillis();
            boolean check = punterBetTimes == null || punterBetTimes.isEmpty() || punterBetTimes.size() < punterLimit.getLimit() ||
                    currentTime - punterBetTimes.get(punterBetTimes.size() - 1).getBetTime() > punterLimit.getTimeLimit();

            result.add(new PunterBetTime(punterLimit.getId(), currentTime, check, punterLimit.getTransactionId()));
        }
        return result;
    }

    private List<PunterLimit> getPunterLimits(Map<Integer, Long> punterIdMap) {
        if (punterIdMap == null || punterIdMap.isEmpty())
            return null;
        RowMapper<PunterLimit> rm = (rs, rowNum) -> {
            PunterLimit punterLimit = new PunterLimit();
            punterLimit.setId(rs.getInt("id"));
            punterLimit.setLimit(rs.getInt("lim"));
            punterLimit.setTimeLimit(rs.getInt("limit_time"));
            punterLimit.setTransactionId(punterIdMap.get(punterLimit.getId()));
            return punterLimit;
        };

        return  jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdMap.size(), SQL_SELECT_PUNTER_LIMITS), rm,
                punterIdMap.keySet().toArray());
    }

    public void deleteBetTimes(List<BetInfo> punterIdList) {
        if (punterIdList == null || punterIdList.isEmpty())
            return;

        jdbcTemplate.batchUpdate(SQL_DELETE_PUNTER_TIMESTAMP, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BetInfo punterBetInfo = punterIdList.get(i);
                ps.setLong(1, punterBetInfo.getTransactionId());
            }

            public int getBatchSize() {
                return punterIdList.size();
            }
        });
    }

    public void insertBatch(List<PunterBetTime> punterBetTimes) {
        if (punterBetTimes == null || punterBetTimes.isEmpty())
            return;

        jdbcTemplate.batchUpdate(SQL_INSERT_PUNTER_TIMESTAMP, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
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

    private void insertPunter(List<BetInfo> punterIdList) {
        if (punterIdList == null || punterIdList.isEmpty())
            return;
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

    public Set<TransactionResult> phase1(Set<BetInfo> punterIdSet) {
        if (punterIdSet == null || punterIdSet.isEmpty())
            return null;
        Map<Integer, Long> punterIdMap = new HashMap<>();
        List<BetInfo> punterIdList = new ArrayList<>();
        punterIdSet.parallelStream().forEach(i -> {
            punterIdMap.put(i.getPunterId(), i.getTransactionId());
            punterIdList.add(i);
        });

        insertPunter(punterIdList);

        List<PunterLimit> punterLimits = getPunterLimits(punterIdMap);
        List<PunterBetTime> result = puntersArbiter(punterLimits);

        insertBatch(result.stream().filter(PunterBetTime::isCheckLimit).collect(Collectors.toList()));
        return result.stream().map((map) -> new TransactionResult(map.getTransactionId(), map.isCheckLimit())).collect(Collectors.toSet());
    }

    public Set<TransactionResult> cancel(List<BetInfo> timestamps) {
        deleteBetTimes(timestamps);
        return new HashSet<TransactionResult>(timestamps.stream().map((map) -> new TransactionResult(map.getTransactionId(), true)).collect(Collectors.toSet()));
    }

    public List<TransactionResult> filterByTable(List<BetInfo> punterIdList) {
        if (punterIdList == null || punterIdList.isEmpty())
            return null;
        RowMapper<TransactionResult> rm = (rs, rowNum) -> {
            TransactionResult transactionResult = new TransactionResult();
            transactionResult.setTransactionId(rs.getLong("transaction_id"));
            transactionResult.setResult(rs.getBoolean("result"));
            return transactionResult;
        };
        return jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdList.size(), SQL_CHECK_IDEMPOTY), rm,
                punterIdList.stream().map(BetInfo::getTransactionId).collect(Collectors.toList()).toArray());
    }

    public void insertFilterTable(List<TransactionResult> transactionResults) {
        if (transactionResults == null || transactionResults.isEmpty())
            return;
        jdbcTemplate.batchUpdate(SQL_INSERT_PUNTER_IDEMP, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TransactionResult transactionResult = transactionResults.get(i);
                ps.setLong(1, transactionResult.getTransactionId());
                ps.setBoolean(2, transactionResult.getResult());
                ps.setLong(3,System.currentTimeMillis());
            }

            public int getBatchSize() {
                return transactionResults.size();
            }
        });
    }

    public void deleteOldData(String tableName, long timeLimit){
       String SQL_DELETE_DATA = "DELETE FROM " + tableName +" WHERE ? - punter_timestamp > ?";
        long currentTime = System.currentTimeMillis();
        jdbcTemplate.update(SQL_DELETE_DATA, currentTime, timeLimit);
    }
}