package ru.splat.Punter;


import ru.splat.facade.AbstractServiceFacade;
import ru.splat.Punter.feautures.PunterInfo;
import ru.splat.Punter.protobuf.PunterReq;
import ru.splat.facade.kafka.KafkaImpl;


public class PunterService extends AbstractServiceFacade<PunterReq.Punter,PunterInfo>
{
    @Override
    public void init()
    {
        setConsumerTimeout(100L);
        setKafka(new KafkaImpl<PunterReq.Punter>("PunterRes","PunterReq", PunterReq.Punter.getDefaultInstance()));
        super.setConverter(consumerRecord -> (new PunterInfo(consumerRecord.value().getPunterId(),
                consumerRecord.key(),consumerRecord.value().getLocalTask())));
        super.startThread();
     }

}
