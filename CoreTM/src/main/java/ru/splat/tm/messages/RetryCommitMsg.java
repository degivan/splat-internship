package ru.splat.tm.messages;

import ru.splat.messages.conventions.ServicesEnum;

import java.util.Set;

/**
 * Created by Дмитрий on 22.02.2017.
 */
public class RetryCommitMsg extends CommitTransactionMsg
{

    public RetryCommitMsg(long transactionId, Set<ServicesEnum> services) {
        super(transactionId, services);
    }
}
