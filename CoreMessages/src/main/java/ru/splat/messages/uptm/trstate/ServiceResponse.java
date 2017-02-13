package ru.splat.messages.uptm.trstate;

import ru.splat.messages.conventions.ServiceResult;
import ru.splat.messages.conventions.TaskTypesEnum;

/**
 * Created by Дмитрий on 02.02.2017.
 */
public class ServiceResponse<T> {
    private final T attachment;
    private final ServiceResult result;
    private final boolean isResponseReceived;
    private boolean requestSent;

    //конструктор для пустого (еще не полученного ответа)
    public ServiceResponse() {

        attachment = null;
        result = null;
        requestSent = false;
        isResponseReceived = false;
    }

    public boolean isResponseReceived() {
        return isResponseReceived;
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
        this.isResponseReceived = true;
        requestSent = true;
    }



    public T getAttachment() {
        return attachment;
    }

    //успешность таски
    public boolean isPositive() {
        return result != null && result.equals(ServiceResult.CONFIRMED);
    }


    public ServiceResult getResult() {
        return result;
    }
}
