package ru.splat.tmprotobuf;

import com.google.protobuf.Message;
import javafx.util.Pair;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trstate.ServiceResponse;

/**
 * Created by Дмитрий on 09.01.2017.
 */
public interface ResponseParser {
    public ServiceResponse unpackMessage(Message message);
}
