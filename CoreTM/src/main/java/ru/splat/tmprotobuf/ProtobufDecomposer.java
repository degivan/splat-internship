package ru.splat.tmprotobuf;

import com.google.protobuf.Message;
import javafx.util.Pair;
import ru.splat.messages.uptm.trmetadata.LocalTask;

/**
 * Created by Дмитрий on 09.01.2017.
 */
public interface ProtobufDecomposer {
    public Pair<Long, LocalTask> unpackMessage(Message message);
}
