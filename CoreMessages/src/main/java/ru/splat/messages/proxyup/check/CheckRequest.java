package ru.ifmo.splat.messages.proxyup.check;

import ru.ifmo.splat.messages.proxyup.IdMessage;

/**
 * ProxyUPMessage for UP asking for bet state.
 */
public class CheckRequest extends IdMessage {
    public CheckRequest(Long transactionId, Long userId) {
        super(transactionId, userId);
    }
}
