package ru.splat.tmactors;

import ru.splat.messages.uptm.trstate.ServiceResponse;

/**
 * Created by Дмитрий on 08.02.2017.
 */
public class ServiceResponseMsg {
    private final ServiceResponse message;
    private final Long transactionId;

    public ServiceResponseMsg(Long transactionId, ServiceResponse message ) {
        this.message = message;
        this.transactionId = transactionId;
    }

    public ServiceResponse getMessage() {
        return message;
    }

    public Long getTransactionId() {
        return transactionId;
    }
}
