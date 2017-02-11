package ru.splat.tm.messages;

import akka.actor.AbstractActor;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trstate.TransactionState;

/**
 * Created by Дмитрий on 18.01.2017.
 */
public class MockRegistry extends AbstractActor {

    public MockRegistry() {
        System.out.println("Registry ready");
    }

    private void processState(TransactionState m) {
        System.out.println("Registry: state received" + m.getTransactionId() +
                " with " + m.getLocalStates().size());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TMResponse.class, m -> System.out.println("Registry: all requests for " + m.getTransactionId()+ " are sent"))
                .match(TransactionState.class, this::processState)
                .build();
    }
}
