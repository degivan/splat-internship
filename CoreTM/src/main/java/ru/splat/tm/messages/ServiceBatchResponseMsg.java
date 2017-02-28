package ru.splat.tm.messages;

import java.util.List;

/**
 * Created by Дмитрий on 27.02.2017.
 */
public class ServiceBatchResponseMsg {
    private final List<ServiceResponseMsg> responseMsgList;

    public ServiceBatchResponseMsg(List<ServiceResponseMsg> responseMsgList) {
        this.responseMsgList = responseMsgList;
    }

    public List<ServiceResponseMsg> getResponseMsgList() {
        return responseMsgList;
    }
}
