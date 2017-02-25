package ru.splat.tm.protobuf;

import com.google.protobuf.Message;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServiceResult;
import ru.splat.messages.uptm.trstate.ServiceResponse;

import static ru.splat.messages.Response.ServiceResponse.AttachmentOneofCase.*;

/**
 * Created by Дмитрий on 09.01.2017.
 */
public class ResponseParser {

    public static ServiceResponse unpackMessage(Response.ServiceResponse message) {
        //if (message instanceof Response.ServiceResponse) {
            int result = message.getResult();

            Enum attachmentCase = message.getAttachmentOneofCase();
            if (attachmentCase.equals(LONGATTACHMENT)) {
                Long attachment = message.getLongAttachment();
                return new ServiceResponse<>(attachment, ServiceResult.values()[result]);
            }
            else if (attachmentCase.equals(STRINGATTACHMENT)) {
                String attachment = message.getStringAttachment();
                return new ServiceResponse<>(attachment, ServiceResult.values()[result]);
            }
            else if (attachmentCase.equals(BOOLEANATTACHMENT)) {
                Boolean attachment = message.getBooleanAttachment();
                return new ServiceResponse<>(attachment, ServiceResult.values()[result]);
            }
            else if (attachmentCase.equals(DOUBLEATTACHMENT)) {
                Double attachment = message.getDoubleAttachment();
                return new ServiceResponse<>(attachment, ServiceResult.values()[result]);
            }
            else if (attachmentCase.equals(ATTACHMENTONEOF_NOT_SET)) {  //для ответов от сервиса, не содержащих аттачмент
                return new ServiceResponse<>(-1, ServiceResult.values()[result]);
            }
            else {
                throw new IllegalArgumentException("Invalid attachment type!");
            }
        //}
        /*else {
            throw new IllegalArgumentException("Invalid message type");
        }*/
    }
}
