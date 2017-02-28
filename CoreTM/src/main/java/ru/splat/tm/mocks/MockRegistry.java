package ru.splat.tm.mocks;

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
    private long firstTime;
    private long lastTime;

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

    private void processTestSent(TMResponse m) {
        if (m.getTransactionId() == 1) { firstTime = System.currentTimeMillis(); log.info("first state received at "); }
        if (m.getTransactionId() == 1000) { lastTime = System.currentTimeMillis(); log.info("difference " + (lastTime - firstTime)); }



    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TMResponse.class, this::processTestSent)
                .match(TransactionState.class, this::processState)
                .match(TransactionStateMsg.class, this::processState)
                .build();
    }
}
