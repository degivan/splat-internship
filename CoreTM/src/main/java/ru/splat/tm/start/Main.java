package ru.splat.tm.start;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import ru.splat.messages.uptm.TMRecoverMsg;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trmetadata.bet.FixBetTask;
import ru.splat.messages.uptm.trmetadata.punter.AddPunterLimitsTask;
import ru.splat.tm.actors.TMActor;
import ru.splat.tm.actors.MockRegistry;
import ru.splat.tm.messages.PollMsg;
import ru.splat.tm.mocks.ServiceMock;
import ru.splat.tm.actors.TMConsumerActor;
import ru.splat.tm.mocks.TickerSelf;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Created by Дмитрий on 07.01.2017.
 */
//заглушка мэйна (согласовать с Иваном)
public class Main {
    private final static Logger LOGGER = Logger.getLogger(TMActor.class);
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("testactors");
        //System.out.println("SETTINGS:");
        //System.out.println(system.settings());
        //ActorRef ticker =  system.actorOf(Props.create(TickerSelf.class).withDispatcher("tm-actor-dispatcher"), "ticker");
        final ActorRef registry = system.actorOf(Props.create(MockRegistry.class), "MockRegistry");
        final ActorRef tmActor = system.actorOf(Props.create(TMActor.class, registry)
                .withDispatcher("my-settings.akka.actor.tm-actor-dispatcher"), "TMActor");
        tmActor.tell(new TMRecoverMsg(), ActorRef.noSender());


        Long time = System.currentTimeMillis();
        LocalTask fixBet1 = new FixBetTask(20L, time);
        LocalTask punterTask1 = new AddPunterLimitsTask(135, time);
        List<LocalTask> tasks = new LinkedList<>(); tasks.add(fixBet1); tasks.add(punterTask1);
        TransactionMetadata transactionMetadata = new TransactionMetadata(111L, tasks);

        tmActor.tell(new TransactionMetadata(111L, tasks), ActorRef.noSender());
        ServiceMock serviceMock = new ServiceMock();
        serviceMock.sendRoutine();
        /*Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
                Duration.create(500, TimeUnit.MILLISECONDS), consumerActor, new PollMsg(),
                system.dispatcher(), null);*/
    }
}
