package ru.splat.actors;

import akka.actor.ActorRef;
import ru.splat.db.Bounds;
import ru.splat.message.RegisterRequest;
import ru.splat.message.RegisterResponse;
import ru.splat.messages.uptm.trstate.TransactionState;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Иван on 21.12.2016.
 */
public class RegistryActor extends LoggingActor {
    private final Map<Bounds, ActorRef> actors;


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RegisterRequest.class, this::processRegisterRequest)
                .match(TransactionState.class, this::processTransactionState)
                .matchAny(this::unhandled).build();

    }

    public RegistryActor(Integer size) {
        actors = new HashMap<>(size);
    }

    private void processTransactionState(TransactionState o) {
        log.info("Processing TransactionState: " + o.toString());

        ActorRef phaser = actors.get(boundsFromTrId(o.getTransactionId()));
        if(phaser == null) {
            log.info("Phaser for transactionId: " + o.getTransactionId() + " wasn't created yet.");

            resendOverDelay(o);
        } else {
            phaser.tell(o, self());
        }
    }

    private static Bounds boundsFromTrId(Long transactionId) {
        Long lowerBound = transactionId - (transactionId % IdGenerator.RANGE);
        Long upperBound = lowerBound + IdGenerator.RANGE;

        return new Bounds(lowerBound, upperBound);
    }

    private void resendOverDelay(TransactionState o) {
        context().system()
                .scheduler()
                .scheduleOnce(
                    Duration.create(500L, TimeUnit.MILLISECONDS),
                    self(), o, context().dispatcher(), sender());
    }

    private void processRegisterRequest(RegisterRequest request) {
        log.info("Processing RegisterRequest: " + request.toString());

        actors.put(request.getBounds(), request.getActor());
        sender().tell(new RegisterResponse(), self());
    }
}
