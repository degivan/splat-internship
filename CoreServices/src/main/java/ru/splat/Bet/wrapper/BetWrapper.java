package ru.splat.Bet.wrapper;

import ru.splat.Bet.feautures.BetInfo;
import ru.splat.facade.wrapper.AbstractWrapper;
import ru.splat.kafka.KafkaImpl;
import ru.splat.messages.BetRequest;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class BetWrapper extends AbstractWrapper<BetRequest.Bet, BetInfo>
{

    private Executor thread = Executors.newSingleThreadExecutor();

    @Override
    public void init()
    {
        setConsumerTimeout(100);
        setKafka(new KafkaImpl<BetRequest.Bet>("BetRes", "BetReq", BetRequest.Bet.getDefaultInstance()));
        setConverter(consumerRecord -> new BetInfo(
                consumerRecord.key(),
                consumerRecord.value().getLocalTask(),
                consumerRecord.value(),
                consumerRecord.value().getServicesList(),
                consumerRecord.value().getId()
        ));
        thread.execute(this::mainProcess);
    }
}
