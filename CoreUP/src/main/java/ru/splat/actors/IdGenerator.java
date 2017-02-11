package ru.splat.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import ru.splat.LoggerGlobal;
import ru.splat.db.Bounds;
import ru.splat.db.DBConnection;
import ru.splat.message.CreateIdRequest;
import ru.splat.message.CreateIdResponse;
import ru.splat.message.NewIdsMessage;
import ru.splat.messages.Transaction;
import ru.splat.messages.Transaction.State;

import java.util.LinkedList;
import java.util.Queue;

import static ru.splat.messages.Transaction.Builder.builder;

/**
 * Puts transaction in DB and generates unique identifier for it.
 */
public class IdGenerator extends AbstractActor {
    private static final Long RANGE = 50L;

    private Queue<CreateIdRequest> adjournedRequests = new LinkedList<>();
    private Bounds bounds = new Bounds(0L, 0L);
    private boolean messagesRequested = false;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CreateIdRequest.class, this::processCreateIdRequest)
                .match(NewIdsMessage.class, this::processNewIdsMessage)
                .matchAny(this::unhandled).build();
    }

    private void processNewIdsMessage(NewIdsMessage message) {
        LoggerGlobal.log("Process NewIdsMessage: " + message.toString());

        bounds = message.getBounds();
        messagesRequested = false;

        while(adjournedRequests.peek() != null &&
              processCreateIdRequest(adjournedRequests.poll())) {}
    }

    private boolean processCreateIdRequest(CreateIdRequest message) {
        LoggerGlobal.log("Process CreateIdRequest: " + message.toString());

        if(outOfIndexes()) {
            LoggerGlobal.log("Out of indexes!");

            adjournedRequests.add(message);
            if(!messagesRequested) {
                DBConnection.createIdentifiers(
                        bounds -> self().tell(new NewIdsMessage(bounds), self()));
                messagesRequested = true;

                LoggerGlobal.log("Messages requested");
            }

            return false;
        } else {
            ActorRef receiver = sender();
            Bounds bounds = getIndexes();
            Transaction transaction = builder()
                    .betInfo(message.getBetInfo())
                    .state(State.CREATED)
                    .lower(bounds.getLowerBound())
                    .upper(bounds.getUpperBound())
                    .build();

            LoggerGlobal.log("Saving new transaction: " + transaction);

            DBConnection.newTransaction(transaction,
                tr -> receiver.tell(new CreateIdResponse(transaction), self()));

            return true;
        }
    }

    private Bounds getIndexes() {
        Bounds b = new Bounds(bounds.getLowerBound(), bounds.getLowerBound() + RANGE);
        bounds = new Bounds(bounds.getLowerBound() + RANGE, bounds.getUpperBound());
        return b;
    }

    private boolean outOfIndexes() {
        return (bounds.getUpperBound() - bounds.getLowerBound() < RANGE);
    }



}
