package ru.splat.tm.messages;

import akka.actor.AbstractActor;
//import org.slf4j.Logger;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trstate.TransactionState;
import org.apache.log4j.Logger;
import ru.splat.tm.LoggerGlobal;

/**
 * Created by Дмитрий on 18.01.2017.
 */
public class MockRegistry extends AbstractActor {
    private final Logger LOGGER = Logger.getLogger(MockRegistry.class);

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
