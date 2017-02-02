package ru.splat.Billing.wrapper;

import ru.splat.Billing.feautures.BillingInfo;
import ru.splat.facade.wrapper.AbstractWrapper;
import ru.splat.kafka.KafkaImpl;
import ru.splat.messages.BillingRequest;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BillingWrapper extends AbstractWrapper<BillingRequest.Billing, BillingInfo>
{

    private Executor thread = Executors.newSingleThreadExecutor();

    @Override
    public void init()
    {
        setConsumerTimeout(100);
        setKafka(new KafkaImpl<BillingRequest.Billing>("BillingRes", "BillingReq", BillingRequest.Billing.getDefaultInstance()));
        setConverter(consumerRecord -> new BillingInfo(
                consumerRecord.value().getPunterId(),
                consumerRecord.value().getSum(),
                consumerRecord.key(),
                consumerRecord.value().getLocalTask(),
                consumerRecord.value().getServicesList()));
        thread.execute(this::mainProcess);
    }
}
