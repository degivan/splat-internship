package ru.splat.messages.uptm.trstate;

import ru.splat.messages.conventions.ServiceResult;

/**
 * Created by Дмитрий on 02.02.2017.
 */
public class ServiceResponse<T> {
    private final T attachment;
    private final ServiceResult result;



    public ServiceResponse(T attachment, ServiceResult result) {
        this.attachment = attachment;
        this.result = result;
    }

    public T getAttachment() {
        return attachment;
    }

    //успешность таски
    public boolean isPositive() {
        return result.equals(ServiceResult.CONFIRMED);
    }


    public ServiceResult getResult() {
        return result;
    }
}
