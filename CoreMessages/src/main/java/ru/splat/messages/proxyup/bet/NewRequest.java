package ru.splat.messages.proxyup.bet;

import ru.splat.messages.proxyup.ProxyUPMessage;

/**
 * ProxyUPMessage for UP asking to place a new bet.
 */
public class NewRequest extends ProxyUPMessage {
    private final BetInfo betInfo;

    public NewRequest(BetInfo betInfo) {
        super(betInfo.getUserId());
        this.betInfo = betInfo;
    }

    public BetInfo getBetInfo() {
        return betInfo;
    }

    @Override
    public String toString() {
        return "NewRequest{" +
                "betInfo=" + betInfo +
                '}';
    }
}
