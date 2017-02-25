package ru.splat.actors;

import akka.actor.ActorRef;
import ru.splat.db.Bounds;
import ru.splat.message.RegisterRequest;
import ru.splat.message.RegisterResponse;
import ru.splat.messages.uptm.trstate.TransactionStateMsg;
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
                .match(TransactionStateMsg.class, this::processTransactionStateMsg)
                .matchAny(this::unhandled).build();

    }

    public RegistryActor(Integer size) {
        actors = new HashMap<>(size);
    }

    private void processTransactionStateMsg(TransactionStateMsg o) {
        log.info("Processing TransactionStateMsg: " + o.toString());

        ActorRef phaser = actors.get(boundsFromTrId(o.getTransactionState().getTransactionId()));
        if(phaser == null) {
            log.info("Phaser for transactionId: " + o.getTransactionState().getTransactionId() + " wasn't created yet.");

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

    private void processRegisterRequest(RegisterRequest request) {
        log.info("Processing RegisterRequest: " + request.toString());

        actors.put(request.getBounds(), request.getActor());
        sender().tell(new RegisterResponse(), self());
    }

    private void resendOverDelay(Object o) {
        log.info("Resending over delay: " + o.toString());

        context().system()
                .scheduler()
                .scheduleOnce(
                        Duration.create(500L, TimeUnit.MILLISECONDS),
                        self(), o, context().dispatcher(), sender());
    }
}
