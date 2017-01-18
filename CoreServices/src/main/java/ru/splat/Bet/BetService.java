package ru.splat.Bet;


import ru.splat.Bet.feautures.BetInfo;
import ru.splat.Bet.feautures.Outcome;
import ru.splat.Bet.protobuf.BetReq;
import ru.splat.Punter.feautures.PunterInfo;
import ru.splat.facade.AbstractServiceFacade;
import ru.splat.facade.kafka.KafkaImpl;

import java.util.stream.Collectors;

public class BetService extends AbstractServiceFacade<BetReq.Bet, BetInfo>
{
    @Override
    public void init() {
        setConsumerTimeout(100);
        setKafka(new KafkaImpl<BetReq.Bet>("BetRes", "BetReq", BetReq.Bet.getDefaultInstance()));
        setConverter(consumerRecord -> new BetInfo(
                consumerRecord.key(),
                consumerRecord.value().getLocalTask(),
                consumerRecord.value().getPunterId(),
                consumerRecord.value().getBetSum(),
                consumerRecord.value().getBetOutcomeList().stream().
                        map(map -> new Outcome(map.getId(),map.getEventId(),map.getKoef())).collect(Collectors.toList())));
        super.startThread();
    }
}
