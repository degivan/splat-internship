package ru.ifmo.splat.messages.proxyup.bet;

import ru.ifmo.splat.messages.proxyup.IdMessage;

/**
 * Answer for NewRequest message.
 */
public class NewResponse extends IdMessage {
    public NewResponse(Long transactionId, Long userId) {
        super(transactionId, userId);
    }
}
