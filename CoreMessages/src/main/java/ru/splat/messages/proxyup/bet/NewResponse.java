package ru.splat.messages.proxyup.bet;

import ru.splat.messages.proxyup.IdMessage;

/**
 * Answer for NewRequest message.
 */
public class NewResponse extends IdMessage {
    private final boolean isActive;
    //конструктор для положительного ответа
    public NewResponse(Long transactionId, Integer userId)
    {
        super(transactionId, userId);
        isActive = false;
    }
    //конструкор для отрицательного ответа о приеме транзакции
    public NewResponse(Integer userId) {
        super(-1L, userId);
        isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }
}
