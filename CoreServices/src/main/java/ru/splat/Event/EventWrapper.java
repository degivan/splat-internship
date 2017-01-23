package ru.splat.Event;


import ru.splat.facade.AbstractWrapper;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventWrapper extends AbstractWrapper{

    private Executor thread = Executors.newSingleThreadExecutor();

    @Override
    public void init()
    {
        /*
        setConsumerTimeout(100L);
        setKafka(new KafkaImpl<PunterRequest.Punter>("PunterRes", "PunterReq", PunterRequest.Punter.getDefaultInstance()));
        super.setConverter(consumerRecord -> (new PunterInfo(
                consumerRecord.value().getPunterId(),
                consumerRecord.key(),
                consumerRecord.value().getLocalTask(),
                consumerRecord.value().getServices()
        )));
        thread.execute(this::mainProcess);
    */
    }

}
