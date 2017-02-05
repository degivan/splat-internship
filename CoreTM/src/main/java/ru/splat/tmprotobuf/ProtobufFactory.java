package ru.splat.tmprotobuf;

import com.google.protobuf.Message;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;

import java.util.Set;

/**
 * Created by Дмитрий on 05.02.2017.
 */
public interface ProtobufFactory {
    public Message buildProtobuf(LocalTask localTask, Set<ServicesEnum> _services) throws IllegalArgumentException;
}
