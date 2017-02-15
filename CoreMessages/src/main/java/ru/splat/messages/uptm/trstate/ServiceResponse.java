package ru.splat.messages.uptm.trstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.splat.messages.conventions.ServiceResult;

/**
 * Created by Дмитрий on 02.02.2017.
 */
public class ServiceResponse<T> {
    private final T attachment;
    private final ServiceResult result;
    private final boolean responseReceived;
    private boolean requestSent;

    //конструктор для пустого (еще не полученного ответа)
    public ServiceResponse() {

        attachment = null;
        result = null;
        requestSent = false;
        responseReceived = false;
    }

    public boolean isResponseReceived() {
        return responseReceived;
    }

    public boolean isRequestSent() {
        return requestSent;
    }
    public void setRequestSent(boolean sent) {
        requestSent = sent;
    }

    //конструктор для полученного ответа (заменяет в мапе пустой)
    public ServiceResponse(T attachment, ServiceResult result) {
        this.attachment = attachment;
        this.result = result;
        this.responseReceived = true;
        requestSent = true;
    }



    public T getAttachment() {
        return attachment;
    }

    //успешность таски
    @JsonIgnore
    public boolean isPositive() {
        return result != null && result.equals(ServiceResult.CONFIRMED);
    }


    public ServiceResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ServiceResponse{" +
                "attachment=" + attachment +
                ", result=" + result +
                ", responseReceived=" + responseReceived +
                ", requestSent=" + requestSent +
                '}';
    }
}
