package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ru.splat.LoggerGlobal;
import ru.splat.message.RegisterRequest;
import ru.splat.message.RegisterResponse;
import ru.splat.messages.uptm.trstate.TransactionState;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by Иван on 21.12.2016.
 */
public class RegistryActor extends UntypedActor {
    private final Map<Long, ActorRef> actors;

    public RegistryActor(Integer size) {
        actors = new HashMap<>(size);
    }

    @Override
    public void onReceive(Object o) throws Throwable {
        if(o instanceof RegisterRequest) {
            processRegisterRequest((RegisterRequest) o);
        } else if(o instanceof TransactionState) {
            processTransactionState((TransactionState) o);
        }
    }

    private void processTransactionState(TransactionState o) {
        LoggerGlobal.log("Processing TransactionState: " + o.toString());

        actors.get(o.getTransactionId())
                .tell(o, getSelf());
    }

    private void processRegisterRequest(RegisterRequest request) {
        LoggerGlobal.log("Processing RegisterRequest: " + request.toString());

        actors.put(request.getTransactionId(), request.getActor());
        getSender().tell(new RegisterResponse(), getSelf());
    }
}
