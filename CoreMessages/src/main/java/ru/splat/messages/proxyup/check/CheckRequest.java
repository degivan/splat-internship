package ru.splat.messages.proxyup.check;

import ru.splat.messages.proxyup.IdMessage;

/**
 * ProxyUPMessage for UP asking for bet state.
 */
public class CheckRequest extends IdMessage {
    public CheckRequest(Long transactionId, Long userId) {
        super(transactionId, userId);
    }
}
