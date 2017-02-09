package ru.splat.tmmessages;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trstate.ServiceResponse;

/**
 * Created by Дмитрий on 08.02.2017.
 */
public class ServiceResponseMsg {
    private final ServiceResponse message;
    private final Long transactionId;
    private final ServicesEnum service;

    public ServiceResponseMsg(Long transactionId, ServiceResponse message, ServicesEnum service) {
        this.message = message;
        this.transactionId = transactionId;
        this.service = service;
    }

    public ServiceResponse getMessage() {
        return message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public ServicesEnum getService() {
        return service;
    }
}
