package ru.splat.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.ExecutionContextExecutor;

/**
 * Contains common fields for different actor types.
 */
public abstract class LoggingActor extends AbstractActor {
    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    final ExecutionContextExecutor executor = context().dispatcher();
}
