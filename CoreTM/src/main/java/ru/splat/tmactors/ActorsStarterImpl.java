package ru.splat.tmactors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import org.springframework.beans.factory.annotation.Autowired;
import ru.splat.tm.*;
import ru.splat.tmkafka.TMConsumer;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Дмитрий on 17.01.2017.
 */
public class ActorsStarterImpl implements ActorsStarter {
    @Autowired
    private TMStarter tmStarter;

    @Autowired
    private TMConsumer tmConsumer;


    @Override
    public void initActors() {
        ActorSystem system = ActorSystem.create("testactors");
        final ActorRef mockRegistry = system.actorOf(Props.create(MockRegistry.class), "mockRegistry");
        final ActorRef tmFinalizer = system.actorOf(Props.create(TMFinalizerImpl.class, mockRegistry), "tmFinalizer");
        final ActorRef tmActor = system.actorOf(Props.create(TMActor.class, tmStarter, tmFinalizer), "tmActor");
        final ActorRef tmConsumerActor = system.actorOf(Props.create(TMConsumerActor.class, tmFinalizer, tmConsumer), "tmConsumerActor");


        //Стартуем консюмер
        Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
                Duration.create(200, TimeUnit.MILLISECONDS), tmConsumerActor, "poll",
                system.dispatcher(), null);

        //создаем 10 тестовых транзакций
        for (Long i = 0L; i < 10L; i++) {
            String actorName = "mockPhaser" + i;
            ActorRef mockPhaser = system.actorOf(Props.create(MockPhaser.class, tmActor), actorName);
            mockPhaser.tell((Object) i, ActorRef.noSender());
        }
    }
    public void setTmStarter(TMStarter tmStarter) {
        this.tmStarter = tmStarter;
    }
    public void setTmConsumer(TMConsumer tmConsumer) {
        this.tmConsumer = tmConsumer;
    }
}
