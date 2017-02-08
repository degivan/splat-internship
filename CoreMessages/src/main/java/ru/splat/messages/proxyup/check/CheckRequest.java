package ru.splat.messages.proxyup.check;

import ru.splat.messages.proxyup.IdMessage;

/**
 * ProxyUPMessage for UP asking for bet state.
 */
public class CheckRequest extends IdMessage {
    public CheckRequest(Long transactionId, Integer userId) {
        super(transactionId, userId);
    }

    @Override
    public String toString() {
        return "CheckRequest{} " + super.toString();
    }
}
