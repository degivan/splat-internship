package ru.splat.tm.messages;

import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trstate.ServiceResponse;

/**
 * Created by Дмитрий on 08.02.2017.
 */
public class ServiceResponseMsg {
    private final ServiceResponse message;
    private final Long transactionId;
    private final ServicesEnum service;
    private final long offset;

    public ServiceResponseMsg(Long transactionId, ServiceResponse message, ServicesEnum service, long offset) {
        this.message = message;
        this.transactionId = transactionId;
        this.service = service;
        this.offset = offset;
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

    public long getOffset() {
        return offset;
    }
}
