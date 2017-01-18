package ru.splat.facade.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
import java.util.function.Supplier;


public class ProtoBufMessageDeserializer<T extends Message> implements Deserializer<T>
{

    private final Supplier<Message.Builder> builderFactory;



    public ProtoBufMessageDeserializer(T instance)
    {
        this.builderFactory = () -> instance.toBuilder().clear();
    }


    @Override
    public void configure(Map configs, boolean isKey)
    {
    }


    @Override
    public T deserialize(String topic, byte[] data)
    {
        try
        {
            //noinspection unchecked
            return (T) builderFactory.get().mergeFrom(data).build();
        }
        catch (InvalidProtocolBufferException e)
        {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close()
    {
    }
}