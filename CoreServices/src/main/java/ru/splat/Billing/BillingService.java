package ru.splat.Billing;


import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.facade.AbstractServiceFacade;
import ru.splat.Billing.protobuf.BallanceReq;
import ru.splat.facade.kafka.KafkaImpl;

public class BillingService extends AbstractServiceFacade<BallanceReq.Billing,BillingInfo>
{
    @Override
    public void init()
    {
        setConsumerTimeout(100);
        setKafka(new KafkaImpl<BallanceReq.Billing>("BillingRes","BillingReq", BallanceReq.Billing.getDefaultInstance()));
        super.setConverter(consumerRecord -> new BillingInfo(consumerRecord.value().getPunterId(),
                consumerRecord.value().getBallance(),consumerRecord.key(),consumerRecord.value().getLocalTask()));
        super.startThread();

    }
}

