package ru.ifmo.splat.messages.proxyup.bet;

import java.util.List;

/**
 * Information about new bet.
 */
public class BetInfo {
    private Long userId;
    private Long bet;
    private List<Long> eventsId;
    private List<Long> selectionsId;

    public Long getBet() {
        return bet;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setBet(Long bet) {
        this.bet = bet;
    }

    public List<Long> getEventsId() {
        return eventsId;
    }

    public void setEventsId(List<Long> eventsId) {
        this.eventsId = eventsId;
    }

    public List<Long> getSelectionsId() {
        return selectionsId;
    }

    public void setSelectionsId(List<Long> selectionsId) {
        this.selectionsId = selectionsId;
    }
}
