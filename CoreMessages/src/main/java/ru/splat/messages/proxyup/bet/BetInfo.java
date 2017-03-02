package ru.splat.messages.proxyup.bet;

import ru.splat.messages.uptm.trmetadata.bet.BetOutcome;

import java.util.Set;

/**
 * Information about new bet.
 */
public class BetInfo {
    private Long betId = -1L; //temporary decision
    private Integer userId;
    private Integer bet;
    private Set<BetOutcome> betOutcomes;
    private Set<Integer> selectionsId;

    public BetInfo() {
    }

    public BetInfo(Long betId, Integer userId, Integer bet, Set<BetOutcome> betOutcomes) {
        this.betId = betId;
        this.userId = userId;
        this.bet = bet;
        this.betOutcomes = betOutcomes;
    }

    public Integer getBet() {
        return bet;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setBet(Integer bet) {
        this.bet = bet;
    }

    public Set<BetOutcome> getBetOutcomes() {
        return betOutcomes;
    }

    public void setBetOutcomes(Set<BetOutcome> betOutcomes) {
        this.betOutcomes = betOutcomes;
    }

    public Set<Integer> getSelectionsId() {
        return selectionsId;
    }

    public void setSelectionsId(Set<Integer> selectionsId) {
        this.selectionsId = selectionsId;
    }

    public Long getBetId() {
        return betId;
    }

    public void setBetId(Long betId) {
        this.betId = betId;
    }

    @Override
    public String toString() {
        return "BetInfo{" +
                "betId=" + betId +
                ", userId=" + userId +
                ", bet=" + bet +
                ", betOutcomes=" + betOutcomes +
                ", selectionsId=" + selectionsId +
                '}';
    }
}
