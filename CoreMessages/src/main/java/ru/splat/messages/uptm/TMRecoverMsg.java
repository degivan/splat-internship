package ru.splat.messages.uptm;

import ru.splat.messages.conventions.ServicesEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Дмитрий on 21.02.2017.
 */

//сообщение, содержащее даннные об уже отправленных транзакциях
public class TMRecoverMsg {
    private final Map<Long, List<ServicesEnum>> transactions;

    public TMRecoverMsg(Map<Long, List<ServicesEnum>> transactions) {
        this.transactions = transactions;
    }

    public TMRecoverMsg() {
        transactions = new HashMap<>();
    }

    public Map<Long, List<ServicesEnum>> getTransactions() {
        return transactions;
    }
}
