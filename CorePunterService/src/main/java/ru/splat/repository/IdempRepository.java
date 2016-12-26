package ru.splat.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.PunterUtil;
import ru.splat.feautures.BetInfo;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class IdempRepository implements IdempRepositoryInterface<BetInfo, TransactionResult> {

    private static final String SQL_INSERT_IDEMP = "INSERT INTO punter_idemp (transaction_id, result, resultreason,  punter_timestamp) VALUES (?, ?, ?, ?)";
    private static final String SQL_CHECK_IDEMP = "SELECT transaction_id, result, resultreason FROM punter_idemp WHERE transaction_id IN (?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;



    @Override
    public void insertFilterTable(List<TransactionResult> transactionResults) {

        System.out.println("Tx : " + TransactionSynchronizationManager.isActualTransactionActive());
        if (transactionResults == null || transactionResults.isEmpty())
            return;

        jdbcTemplate.batchUpdate(SQL_INSERT_IDEMP, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TransactionResult transactionResult = transactionResults.get(i);
                ps.setLong(1, transactionResult.getTransactionId());
                ps.setBoolean(2, transactionResult.getResult());
                ps.setString(3, transactionResult.getResultReason());
                ps.setLong(4,System.currentTimeMillis());
            }

            public int getBatchSize() {
                return transactionResults.size();
            }
        });
    }

    @Override
    public List<TransactionResult> filterByTable(List<BetInfo> punterIdList) {
        if (punterIdList == null || punterIdList.isEmpty())
            return null;

        RowMapper<TransactionResult> rm = (rs, rowNum) -> {
            TransactionResult transactionResult = new TransactionResult();
            transactionResult.setTransactionId(rs.getLong("transaction_id"));
            transactionResult.setResult(rs.getBoolean("result"));
            transactionResult.setResultReason(rs.getString("resultreason"));
            return transactionResult;
        };
        return jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdList.size(), SQL_CHECK_IDEMP), rm,
                punterIdList.stream().map(BetInfo::getTransactionId).collect(Collectors.toList()).toArray());
    }


}
