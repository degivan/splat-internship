package ru.splat.message;

import ru.splat.messages.proxyup.bet.BetInfo;

/**
 * Message from receiver to id_generator.
 */
public class CreateIdRequest implements InnerMessage {
    private final BetInfo betInfo;

    public CreateIdRequest(BetInfo betInfo) {
        this.betInfo = betInfo;
    }

    public BetInfo getBetInfo() {
        return betInfo;
    }

    @Override
    public String toString() {
        return "CreateIdRequest{" +
                "betInfo=" + betInfo +
                '}';
    }
}
