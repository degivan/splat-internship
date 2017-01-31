package ru.ifmo.splat.messages.proxyup.bet;

import ru.ifmo.splat.messages.proxyup.ProxyUPMessage;

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
}
