package ru.splat.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import ru.splat.LoggerGlobal;
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
public class RegistryActor extends AbstractActor {
    private final Map<Long, ActorRef> actors;


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
        LoggerGlobal.log("Processing TransactionState: " + o.toString());

        ActorRef phaser = actors.get(o.getTransactionId());
        if(phaser == null) {
            LoggerGlobal.log("Phaser for transactionId: " + o.getTransactionId() + " wasn't created yet.");

            resendOverDelay(o);
        } else {
            phaser.tell(o, self());
        }
    }

    private void resendOverDelay(TransactionState o) {
        context().system()
                .scheduler()
                .scheduleOnce(
                    Duration.create(500L, TimeUnit.MILLISECONDS),
                    self(), o, context().dispatcher(), ActorRef.noSender());
    }

    private void processRegisterRequest(RegisterRequest request) {
        LoggerGlobal.log("Processing RegisterRequest: " + request.toString());

        actors.put(request.getTransactionId(), request.getActor());
        sender().tell(new RegisterResponse(), self());
    }


}
