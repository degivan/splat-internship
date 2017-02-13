package ru.splat.tm.protobuf;

import com.google.protobuf.Message;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServiceResult;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.tm.LoggerGlobal;

import static ru.splat.messages.Response.ServiceResponse.AttachmentOneofCase.*;

/**
 * Created by Дмитрий on 09.01.2017.
 */
public class ResponseParser {

    public static ServiceResponse unpackMessage(Message message) {
        if (message instanceof Response.ServiceResponse) {
            Enum attachmentCase = ((Response.ServiceResponse) message).getAttachmentOneofCase();
            if (attachmentCase.equals(LONGATTACHMENT)) {
                Long attachment = ((Response.ServiceResponse) message).getLongAttachment();
                int result = ((Response.ServiceResponse) message).getResult();
                ServiceResponse<Long> sr = new ServiceResponse<>(attachment, ServiceResult.values()[result]);
                return sr;
            }
            else if (attachmentCase.equals(STRINGATTACHMENT)) {
                String attachment = ((Response.ServiceResponse) message).getStringAttachment();
                int result = ((Response.ServiceResponse) message).getResult();
                ServiceResponse<String> sr = new ServiceResponse<>(attachment, ServiceResult.values()[result]);
                return sr;
            }
            else if (attachmentCase.equals(BOOLEANATTACHMENT)) {
                Boolean attachment = ((Response.ServiceResponse) message).getBooleanAttachment();
                int result = ((Response.ServiceResponse) message).getResult();
                ServiceResponse<Boolean> sr = new ServiceResponse<>(attachment, ServiceResult.values()[result]);
                return sr;
            }
            else if (attachmentCase.equals(DOUBLEATTACHMENT)) {
                Double attachment = ((Response.ServiceResponse) message).getDoubleAttachment();
                int result = ((Response.ServiceResponse) message).getResult();
                ServiceResponse<Double> sr = new ServiceResponse<>(attachment, ServiceResult.values()[result]);
                return sr;
            }
            else if (attachmentCase.equals(ATTACHMENTONEOF_NOT_SET)) {  //для ответов от сервиса, не содержащих аттачмент
                int result = ((Response.ServiceResponse) message).getResult();
                ServiceResponse<Integer> sr = new ServiceResponse<>(-1, ServiceResult.values()[result]);
                return sr;
            }
            else {
                LoggerGlobal.log(attachmentCase.toString());
                throw new IllegalArgumentException("Invalid attachment type!");
            }

        }
        else {
            throw new IllegalArgumentException("Invalid message type");
        }
    }
}
