package ru.splat.tmstart;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trmetadata.bet.FixBetTask;
import ru.splat.messages.uptm.trmetadata.punter.AddPunterLimitsTask;
import ru.splat.mocks.ServiceMock;
import ru.splat.tmactors.TMActor;
import ru.splat.tmactors.TMConsumerActor;
import ru.splat.tmmessages.MockRegistry;
import ru.splat.tmmessages.PollMsg;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Дмитрий on 07.01.2017.
 */
//заглушка мэйна (согласовать с Иваном)
public class Main {
    public static void main(String[] args) {
        /*ApplicationContext appContext =
                new ClassPathXmlApplicationContext("beans.xml");*/
        ServiceMock serviceMock = new ServiceMock();
        serviceMock.sendRoutine();
        ActorSystem system = ActorSystem.create("testactors");
        final ActorRef registry = system.actorOf(Props.create(MockRegistry.class), "MockRegistry");
        final ActorRef tmActor = system.actorOf(Props.create(TMActor.class, registry), "TMActor");
        final ActorRef consumerActor = system.actorOf(Props.create(TMConsumerActor.class, tmActor), "TMConsumerActor");

        Long time = System.currentTimeMillis();
        LocalTask fixBet1 = new FixBetTask(20L, time);
        LocalTask punterTask1 = new AddPunterLimitsTask(135, time);
        List<LocalTask> tasks = new LinkedList<>(); tasks.add(fixBet1); tasks.add(punterTask1);
        TransactionMetadata transactionMetadata = new TransactionMetadata(111L, tasks);

        tmActor.tell(new TransactionMetadata(111L, tasks), ActorRef.noSender());

        Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
                Duration.create(4000, TimeUnit.MILLISECONDS), consumerActor, new PollMsg(),
                system.dispatcher(), null);

        /*Cancellable taskLoop = system.scheduler().schedule(Duration.Zero(),
                Duration.create(6000, TimeUnit.MILLISECONDS), tmActor, new TaskSentMsg(111L, ServicesEnum.BetService),
                system.dispatcher(), null);*/

        System.out.println("Hello");


    }
}
