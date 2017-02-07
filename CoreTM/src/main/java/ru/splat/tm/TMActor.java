package ru.splat.tm;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.tmactors.SendBatchMessage;


/**
 * Created by Дмитрий on 05.01.2017.
 */
public  class TMActor extends UntypedActor {

    private TMStarter tmStarter;
    private ActorRef tmFinalizer;

    public TMActor(TMStarter tmStarter, ActorRef tmFinalizer) {
        this.tmStarter = tmStarter;
        this.tmFinalizer = tmFinalizer;
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof TransactionMetadata) {
            tmFinalizer.tell(message, getSelf());
            tmStarter.processTransaction((TransactionMetadata) message);
        }
        if (message instanceof SendBatchMessage) {
            tmStarter.sendBatch();
        }
        else {
            unhandled(message);
        }

    }
}


