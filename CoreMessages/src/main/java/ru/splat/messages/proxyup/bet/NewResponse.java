package ru.splat.messages.proxyup.bet;

import ru.splat.messages.proxyup.IdMessage;

/**
 * Answer for NewRequest message.
 */
public class NewResponse extends IdMessage {
    public NewResponse(Long transactionId, Integer userId) {
        super(transactionId, userId);
    }
}
