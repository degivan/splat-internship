package ru.splat.actors;

import akka.actor.ActorRef;
import ru.splat.db.Bounds;
import ru.splat.message.DeleteRefMsg;
import ru.splat.message.RegisterRequest;
import ru.splat.message.RegisterResponse;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trstate.TransactionStateMsg;

import java.util.HashMap;
import java.util.Map;

/**
 * Buffer between UP phasers and TMActor.
 */
public class RegistryActor extends LoggingActor {
    private final Map<Bounds, ActorRef> actors;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RegisterRequest.class, this::processRegisterRequest)
                .match(TMResponse.class, m -> sendToPhaser(m, m.getTransactionId()))
                .match(TransactionStateMsg.class, m -> sendToPhaser(m, m.getTransactionState().getTransactionId()))
                .match(DeleteRefMsg.class, m -> actors.remove(m.getBounds()))
                .matchAny(this::unhandled).build();

    }

    public RegistryActor(Integer size) {
        actors = new HashMap<>(size);
    }

    private void sendToPhaser(Object message, Long transactionId) {
        log.info("Processing " + message.toString());

        ActorRef phaser = actors.get(boundsFromTrId(transactionId));

        if(phaser == null) {
            log.info("Phaser for transactionId: " + transactionId + " already dead.");
        } else {
            phaser.tell(message, self());
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
}
