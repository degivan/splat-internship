package ru.splat.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created by Иван on 17.02.2017.
 */
public abstract class LoggingActor extends AbstractActor {
    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    protected void execute(Runnable runnable) {
        context().dispatcher().execute(runnable);
    }
}
