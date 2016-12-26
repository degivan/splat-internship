package ru.splat.Billing.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.Billing.feautures.PunterBallance;
import ru.splat.Billing.feautures.TransactionResult;
import ru.splat.Billing.protobuf.BallanceRes;
import ru.splat.PunterUtil;
import ru.splat.feautures.BetInfo;
import ru.splat.feautures.PunterBetTime;
import ru.splat.feautures.PunterLimit;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class BillingRepository {

    private static int DEFAULT_ZERO = 0;
    private static int DEFAULT_SUM = 1000;

    public BillingRepository()
    {

    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT_PUNTER_BALLANCE = "INSERT INTO ballance (sum, bets_count, bets_sum, punter_id) SELECT ?,?,?,?"
                    +" WHERE NOT EXISTS (SELECT 1 FROM ballance WHERE ballance.punter_id = ?)";


    private static final String SQL_UPDATE_BALLANCE= "update ballance set sum = sum - ?, bets_count = bets_count + ?,"
            + " bets_sum = bets_sum + ? where punter_id = ?";


    private static final String SQL_SELECT_FILTER_BALLANCE = "SELECT punter_id, sum FROM ballance WHERE punter_id IN (?)";

    public Set<TransactionResult> phase1(List<BillingInfo> punterIdList) {
        insertPunterBallance(punterIdList.stream().map(billingInfo -> billingInfo.getPunterID()).collect(Collectors.toList()));
        boolean check=true;
        List<PunterBallance> ballance = getPunterBallance(punterIdList.stream().map(billingInfo -> billingInfo.getPunterID()).collect(Collectors.toList()));
        Map<Integer,Integer> map = new HashMap<>();
        ballance.stream().forEach(punterBallance -> map.put(punterBallance.getPunterId(),punterBallance.getSum()));
        Set<TransactionResult> transactionResults = new HashSet<>();
        List<BillingInfo> forPay = new ArrayList<>();
        for (BillingInfo billingInfo:punterIdList) {
            if (map.get(billingInfo.getPunterID())>=billingInfo.getSum()){
                transactionResults.add(new TransactionResult(billingInfo.getTransactionId(),true,"Sucessefull"));
                forPay.add(billingInfo);
            }
            else {
                transactionResults.add(new TransactionResult(billingInfo.getTransactionId(),false,"No money"));
            }
        }
        pay(forPay,check);
        return transactionResults;
    }
    public Set<TransactionResult> cancel(List<BillingInfo> punterIdList){
        boolean check = false;
        pay(punterIdList,check);
        return punterIdList.stream().map(billingInfo -> new TransactionResult(billingInfo.getTransactionId(),true,"Sucessefull")).collect(Collectors.toSet());
    }

    private void insertPunterBallance(List<Integer> billingInfoList) {
        if (billingInfoList == null || billingInfoList.isEmpty())
            return;
        jdbcTemplate.batchUpdate(SQL_INSERT_PUNTER_BALLANCE, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, DEFAULT_SUM);
                ps.setInt(2, DEFAULT_ZERO);
                ps.setLong(3, DEFAULT_ZERO);
                ps.setInt(4, billingInfoList.get(i));
                ps.setInt(5, billingInfoList.get(i));
            }

            public int getBatchSize() {
                return billingInfoList.size();
            }
        });
    }

    private List<PunterBallance> getPunterBallance(List<Integer> punterIdList) {
        if (punterIdList == null || punterIdList.isEmpty())
            return null;

        RowMapper<PunterBallance> rm = (rs, rowNum) -> {
            PunterBallance punterBallance = new PunterBallance();
            punterBallance.setPunterId(rs.getInt("punter_id"));
            punterBallance.setSum(rs.getInt("sum"));
            return punterBallance;
        };

        return  jdbcTemplate.query(
                PunterUtil.addSQLParametrs(punterIdList.size(), SQL_SELECT_FILTER_BALLANCE), rm,
                punterIdList.toArray());
    }



    private void pay(List<BillingInfo> billingInfoList,boolean inverse)
    {
        int index = inverse?1:-1;
        jdbcTemplate.batchUpdate(SQL_UPDATE_BALLANCE, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BillingInfo billingInfo = billingInfoList.get(i);
                ps.setInt(1, index*billingInfo.getSum());
                ps.setInt(2, index);
                ps.setInt(3, index*billingInfo.getSum());
                ps.setInt(4, billingInfo.getPunterID());
            }

            public int getBatchSize() {
                return billingInfoList.size();
            }
        });
    }



}
