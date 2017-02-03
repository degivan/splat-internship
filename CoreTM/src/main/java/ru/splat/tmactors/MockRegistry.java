package ru.splat.tmactors;

import akka.actor.UntypedActor;
import ru.splat.messages.uptm.trstate.TransactionState;

/**
 * Created by Дмитрий on 18.01.2017.
 */
public class MockRegistry extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof TransactionState) {
            TransactionState trState = (TransactionState) message;
            System.out.println("Transaction " + trState.getTransactionId());
            /*for (localState : trState
                 ) {

            }*/
        }
        else {
            unhandled(message);
        }
    }
}
