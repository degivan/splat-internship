package ru.splat.Bet.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import ru.splat.Bet.feautures.BetInfo;
import ru.splat.Bet.feautures.RepAnswerAddBet;
import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.facade.feautures.RepAnswerNothing;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class BetRepository
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    private int getCurrentSequenceVal()
    {
        String SQL_SELECT_CURRVAL = "SELECT nextval('bet_id_seq')";
        RowMapper<Integer> rm = (rs, rowNum) -> rs.getInt(1);
        return jdbcTemplate.query(SQL_SELECT_CURRVAL, rm).get(0);
    }

    public Set<RepAnswerAddBet> addBet(List<BetInfo> betInfoList)
    {
        if (betInfoList == null || betInfoList.isEmpty())
            return null;

        int sequence = getCurrentSequenceVal();

        String SQL_INSERT_BET = "INSERT INTO bet (id, blob, bet_state) VALUES (nextval('bet_id_seq'), ?,CAST (? as state))";

        jdbcTemplate.batchUpdate(SQL_INSERT_BET, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BetInfo betInfo = betInfoList.get(i);
                ps.setBytes(1, betInfo.getBlob().toByteArray());
                ps.setString(2, "UNDEFINED");
            }
            public int getBatchSize() {
                return betInfoList.size();
            }
        });

        Set<RepAnswerAddBet> repAnswers = new HashSet<>(betInfoList.size());
        for (BetInfo betInfo: betInfoList)
        {
            sequence++;
            repAnswers.add(new RepAnswerAddBet(betInfo.getTransactionId(),sequence,betInfo.getServices()));
        }
        return repAnswers;
    }

    public Set<RepAnswerNothing> fixBetState(List<BetInfo> betInfoList, String state)
    {
        if (betInfoList == null || betInfoList.isEmpty())
            return null;
        String SQL_CANCEL_BET = "UPDATE bet SET bet_state = CAST (? AS state) where id = ?";
        jdbcTemplate.batchUpdate(SQL_CANCEL_BET, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BetInfo betInfo = betInfoList.get(i);
                ps.setString(1,state);
                ps.setLong(2, betInfo.getId());
            }

            public int getBatchSize() {
                return betInfoList.size();
            }
        });

        return betInfoList.stream().map(map -> new RepAnswerNothing(map.getTransactionId(),map.getServices())).
                collect(Collectors.toSet());
    }


}
