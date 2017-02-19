package ru.splat.actors;

import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Иван on 19.02.2017.
 */
public abstract class ResendingActor extends LoggingActor {
    protected void resendOverDelay(Object o) {
        log.info("Resending over delay: " + o.toString());

        context().system()
                .scheduler()
                .scheduleOnce(
                        Duration.create(500L, TimeUnit.MILLISECONDS),
                        self(), o, context().dispatcher(), sender());
    }
}
