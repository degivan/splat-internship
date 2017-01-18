package ru.splat.facade.repository;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Punter.protobuf.PunterRes;
import ru.splat.facade.feautures.TransactionResult;
import ru.splat.facade.util.PunterUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;


public class ExactlyOnceRepositoryImpl implements ExactlyOnceRepositoryInterface<TransactionResult> {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private String tableName;

    @Transactional
    @Override
    public void insertFilterTable(List<TransactionResult> transactionResults)
    {
        if (transactionResults == null || transactionResults.isEmpty())
            return;


        String SQL_INSERT_IDEMP = "INSERT INTO" + tableName + " (transaction_id, blob, record_timestamp) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(SQL_INSERT_IDEMP, new BatchPreparedStatementSetter()
        {

            public void setValues(PreparedStatement ps, int i) throws SQLException
            {
                TransactionResult transactionResult = transactionResults.get(i);
                ps.setLong(1, transactionResult.getTransactionId());
                ps.setBytes(2, transactionResult.getResult().toByteArray());
                ps.setLong(3,System.currentTimeMillis());
            }

            public int getBatchSize() {
                return transactionResults.size();
            }
        });
    }

    @Override
    public List<TransactionResult> filterByTable(List<Long> punterIdList)
    {
        if (punterIdList == null || punterIdList.isEmpty())
            return null;

        RowMapper<TransactionResult> rm = (rs, rowNum) ->
        {
            TransactionResult transactionResult = new TransactionResult();
            transactionResult.setTransactionId(rs.getLong("transaction_id"));
            try
            {
                PunterRes.Punter punter = PunterRes.Punter.parseFrom(rs.getBytes("blob"));
                transactionResult.setResult(punter);
            } catch (InvalidProtocolBufferException e)
            {
                e.printStackTrace();
            }
            return transactionResult;
        };

        String SQL_CHECK_IDEMP = "SELECT transaction_id, blob FROM" + tableName + " WHERE transaction_id IN (?)";

        return jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdList.size(), SQL_CHECK_IDEMP), rm,
                punterIdList.stream().collect(Collectors.toList()).toArray());
    }

    @Required
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
