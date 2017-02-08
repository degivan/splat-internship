package ru.splat.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.tm.AbsActor;
import ru.splat.tmactors.MockRegistry;
import ru.splat.tmactors.TaskSent;
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
        final ActorRef absActor = system.actorOf(Props.create(AbsActor.class), "absActor");
        Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
                Duration.create(200, TimeUnit.MILLISECONDS), absActor, new TaskSent(1L, TaskTypesEnum.ADD_BET),
                system.dispatcher(), null);
        /*ActorsStarter actorsStarter = (ActorsStarter) appContext.getBean("ActorsStarter");
        actorsStarter.initActors();*/
        System.out.println("Hello");


    }
}
