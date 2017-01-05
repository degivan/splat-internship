package ru.splat.repository;


import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.PunterUtil;
import ru.splat.protobuf.PunterRes;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ExactlyOnceRepository implements ExactlyOnceRepositoryInterface<TransactionResult>
{

    private static final String SQL_INSERT_IDEMP = "INSERT INTO punter_idemp (transaction_id, blob, punter_timestamp) VALUES (?, ?, ?)";
    private static final String SQL_CHECK_IDEMP = "SELECT transaction_id, blob FROM punter_idemp WHERE transaction_id IN (?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;



    @Transactional
    @Override
    public void insertFilterTable(List<TransactionResult> transactionResults) {
        if (transactionResults == null || transactionResults.isEmpty())
            return;


        jdbcTemplate.batchUpdate(SQL_INSERT_IDEMP, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TransactionResult transactionResult = transactionResults.get(i);
             //   PunterRes.Punter punter = PunterRes.Punter.newBuilder().setResultReason(transactionResult.getResultReason()).setResult(transactionResult.getResult()).setTransactionID(transactionResult.getTransactionId()).build();
                ps.setLong(1, transactionResult.getTransactionId());
                ps.setBytes(2, transactionResult.getPunter().toByteArray());
               // System.out.println(punter.toString());
                ps.setLong(3,System.currentTimeMillis());
            }

            public int getBatchSize() {
                return transactionResults.size();
            }
        });
    }

    @Override
    public List<TransactionResult> filterByTable(List<Long> punterIdList) {
        if (punterIdList == null || punterIdList.isEmpty())
            return null;


        RowMapper<TransactionResult> rm = (rs, rowNum) -> {
            TransactionResult transactionResult = new TransactionResult();
            transactionResult.setTransactionId(rs.getLong("transaction_id"));
            try {
                PunterRes.Punter punter = PunterRes.Punter.parseFrom(rs.getBytes("blob"));
                transactionResult.setPunter(punter);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return transactionResult;
        };
        return jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdList.size(), SQL_CHECK_IDEMP), rm,
                punterIdList.stream().collect(Collectors.toList()).toArray());
    }


}
