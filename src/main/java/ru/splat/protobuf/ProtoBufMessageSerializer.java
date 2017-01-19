package ru.splat.protobuf;

import com.google.protobuf.Message;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;


public class ProtoBufMessageSerializer implements Serializer<Message>
{

    @Override
    public void configure(Map configs, boolean isKey)
    {

    }


    @Override
    public byte[] serialize(String topic, Message data)
    {
        return data == null ? null : data.toByteArray();
    }


    @Override
    public void close()
    {

    }
}