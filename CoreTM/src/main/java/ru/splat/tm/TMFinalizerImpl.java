package ru.splat.tm;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.protobuf.Message;
import ru.splat.messages.conventions.ServiceResponse;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trstate.TransactionState;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Дмитрий on 11.12.2016.
 */
public class TMFinalizerImpl extends UntypedActor implements TMFinalizer {
    private Map<Long, TransactionState> transactionStates;
    private ActorRef registry;

    public TMFinalizerImpl(ActorRef registry) {
        this.registry = registry;
        transactionStates = new HashMap<>();
    }


    private  Boolean stateExists(Long transactionId) {
        return transactionStates.containsKey(transactionId);
    }

    public void createTransactionState(Long transactionId, Map<TaskTypesEnum, ServiceResponse> localStates) {
        /*if (!stateExists(transactionId)) {
            System.out.println("Creating state for: " + transactionId);
            TransactionState transactionState = new TransactionState(transactionId, localStates);
            transactionStates.put(transactionId, transactionState);
        }
        else {//убрать
            System.out.println("State for " + transactionId + " already exists!");
        }*/
    }

    public void onReceive(Object message) throws Exception {
       /*f (message instanceof TransactionMetadata) {
            createTransactionState(((TransactionMetadata) message).getTransactionId(),
                    getLocalStates((TransactionMetadata)message));
        }
        if (message instanceof Message) {
            //Pair<Long, LocalTask>
            //createTransactionState();
        }
        else {
            unhandled(message);
        }
        //if (message instanceof )*/
    }

    /*private Map<TaskTypesEnum, ServiceResponse> getLocalStates(TransactionMetadata trMetadata) {
        Map<TaskTypesEnum, ServiceResponse> localStates = new HashMap<>();
        trMetadata.getLocalTasks().forEach(localTask -> localStates.put(localTask.getType(), ServiceResponse.PROCESSING));
        return localStates;
    }*/
}
