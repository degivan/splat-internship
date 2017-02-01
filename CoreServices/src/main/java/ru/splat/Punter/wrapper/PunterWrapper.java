package ru.splat.Punter.wrapper;

import ru.splat.Punter.feautures.PunterInfo;
import ru.splat.facade.wrapper.AbstractWrapper;
import ru.splat.kafka.KafkaImpl;
import ru.splat.messages.PunterRequest;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PunterWrapper extends AbstractWrapper<PunterRequest.Punter, PunterInfo>
{

    private Executor thread = Executors.newSingleThreadExecutor();

    @Override
    public void init()
    {
        setConsumerTimeout(100L);
        setKafka(new KafkaImpl<PunterRequest.Punter>("PunterRes", "PunterReq", PunterRequest.Punter.getDefaultInstance()));
        super.setConverter(consumerRecord -> (new PunterInfo(
                consumerRecord.value().getPunterId(),
                consumerRecord.key(),
                consumerRecord.value().getLocalTask(),
                consumerRecord.value().getServicesList(),
                consumerRecord.value().getTime()
        )));
        thread.execute(this::mainProcess);
    }
}
