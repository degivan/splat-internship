package ru.splat.tm.mocks;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Дмитрий on 20.02.2017.
 */
public class TickerSelf extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() {
        getContext().system().scheduler().scheduleOnce(
                Duration.create(500, TimeUnit.MILLISECONDS),
                getSelf(), "tick", getContext().system().dispatchers().lookup("tm-actor-dispatcher"), null);
        log.info("tick 1");
    }

    // override postRestart so we don't call preStart and schedule a new message
    @Override
    public void postRestart(Throwable reason) {
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message.equals("tick")) {
            // send another periodic tick after the specified delay
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(1, TimeUnit.SECONDS),
                    getSelf(), "tick", getContext().system().dispatchers().lookup("tm-actor-dispatcher"), null);
            log.info("next tick");
        }
        else {
            unhandled(message);
        }
    }
}