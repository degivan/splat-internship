package ru.splat.tm.mocks;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trmetadata.bet.AddBetTask;
import ru.splat.messages.uptm.trmetadata.bet.BetOutcome;
import ru.splat.messages.uptm.trmetadata.billing.BillingWithdrawTask;
import ru.splat.messages.uptm.trmetadata.event.AddSelectionLimitsTask;
import ru.splat.messages.uptm.trmetadata.punter.AddPunterLimitsTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Дмитрий on 17.01.2017.
 */
//нагрузочное тестирование тестирование ТМ

public class MockPhaser extends AbstractActor {
    private ActorRef tmActor;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private long counter = 1;

    public MockPhaser(ActorRef tmActor) {
        this.tmActor = tmActor;
        //transactionRoutine();
    }


    private void transactionRoutine() {
        long time = System.currentTimeMillis();
        LocalTask punterTask = new AddPunterLimitsTask(135, time);
        BetOutcome betOutcome1 = new BetOutcome(4, 5, 4, 1.2414142);
        BetOutcome betOutcome2 = new BetOutcome(4, 5, 4, 1.2414142);
        Set<BetOutcome> betOutcomeList = new HashSet<BetOutcome>();
        LocalTask betTask = new AddBetTask(135, betOutcomeList, time); Set<Integer> set = new HashSet<>(); set.add(13); set.add(14); set.add(15);
        LocalTask eventTask = new AddSelectionLimitsTask(set, time);
        LocalTask billingTask = new BillingWithdrawTask(14, 100, time);
        List<LocalTask> localTasks = new ArrayList<>(); localTasks.add(betTask); localTasks.add(billingTask); localTasks.add(eventTask); localTasks.add(punterTask);

        for (long trId = 0; trId <= 10000; trId++) {
            TransactionMetadata transactionMetadata = new TransactionMetadata(trId, localTasks);
            tmActor.tell(transactionMetadata, getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateTransactionMsg.class, this::processTransactionMsg)
                .matchAny(this::unhandled)
                .build();
    }

    private void processTransactionMsg(CreateTransactionMsg m) {
        transactionRoutine();

    }

     public static class CreateTransactionMsg {

    }
}
