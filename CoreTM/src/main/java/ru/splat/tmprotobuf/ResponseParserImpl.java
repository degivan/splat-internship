package ru.splat.tmprotobuf;

import com.google.protobuf.Message;
import javafx.util.Pair;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServiceResult;
import ru.splat.messages.uptm.trstate.ServiceResponse;

/**
 * Created by Дмитрий on 09.01.2017.
 */
public class ResponseParserImpl implements ResponseParser {
    @Override
    public ServiceResponse unpackMessage(Message message) {
        //if (message instanceof Response.ServiceResponse) {
            Object attachment = ((Response.ServiceResponse) message).getAttachmentOneofCase();
            return new ServiceResponse(attachment, ServiceResult.CONFIRMED);
       // }
        //else
    }
}
