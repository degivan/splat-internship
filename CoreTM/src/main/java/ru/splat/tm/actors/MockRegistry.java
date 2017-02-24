package ru.splat.tm.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trstate.TransactionState;
import ru.splat.messages.uptm.trstate.TransactionStateMsg;

//import org.slf4j.Logger;




/**
 * Created by Дмитрий on 18.01.2017.
 */
public class MockRegistry extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public MockRegistry() {
        log.info("Registry ready");
    }

    private void processState(TransactionState m) {
        log.info("Registry: state received" + m.getTransactionId() +
                " with " + m.getLocalStates().size());
    }

    private void processState(TransactionStateMsg m) {
        log.info("Registry: state received" + m.getTransactionState().getTransactionId());
        m.getCommitTransaction().run();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TMResponse.class, m -> log.info("Registry: all requests for " + m.getTransactionId()+ " are sent"))
                .match(TransactionState.class, this::processState)
                .match(TransactionStateMsg.class, this::processState)
                .build();
    }
}
