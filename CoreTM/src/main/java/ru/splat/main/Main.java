package ru.splat.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.tm.AbsActor;
import ru.splat.tm.TMActor;
import ru.splat.tm.TMConsumerActor;
import ru.splat.tmactors.MockRegistry;
import ru.splat.tmactors.PollMsg;
import ru.splat.tmactors.TaskSent;
import ru.splat.tmprotobuf.ResponseParser;
import ru.splat.tmprotobuf.ResponseParserImpl;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Дмитрий on 07.01.2017.
 */
//заглушка мэйна (согласовать с Иваном)
public class Main {
    public static void main(String[] args) {
        /*ApplicationContext appContext =
                new ClassPathXmlApplicationContext("beans.xml");*/

        ActorSystem system = ActorSystem.create("testactors");
        final ActorRef tmActor = system.actorOf(Props.create(TMActor.class), "TMActor");
        final ActorRef consumerActor = system.actorOf(Props.create(TMConsumerActor.class, tmActor, new ResponseParserImpl()), "TMConsumerActor");
        Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
                Duration.create(500, TimeUnit.MILLISECONDS), consumerActor, new PollMsg(),
                system.dispatcher(), null);
        /*ActorsStarter actorsStarter = (ActorsStarter) appContext.getBean("ActorsStarter");
        actorsStarter.initActors();*/
        System.out.println("Hello");


    }
}
