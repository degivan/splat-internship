package ru.splat.tm.messages;

import akka.actor.AbstractActor;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trstate.TransactionState;

/**
 * Created by Дмитрий on 18.01.2017.
 */
public class MockRegistry extends AbstractActor {

    public MockRegistry() {
        LoggerGlobal.log("Registry ready");
    }

    private void processState(TransactionState m) {
        LoggerGlobal.log("Registry: state received" + m.getTransactionId() +
                " with " + m.getLocalStates().size());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TMResponse.class, m -> LoggerGlobal.log("Registry: all requests for " + m.getTransactionId()+ " are sent"))
                .match(TransactionState.class, this::processState)
                .build();
    }
}
