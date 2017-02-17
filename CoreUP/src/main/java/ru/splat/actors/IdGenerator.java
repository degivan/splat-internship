package ru.splat.actors;

import akka.actor.ActorRef;
import ru.splat.db.Bounds;
import ru.splat.db.DBConnection;
import ru.splat.message.CreateIdRequest;
import ru.splat.message.CreateIdResponse;
import ru.splat.message.NewIdsMessage;
import ru.splat.messages.Transaction;
import ru.splat.messages.Transaction.State;

import java.util.HashMap;
import java.util.Map;

import static ru.splat.messages.Transaction.Builder.builder;

/**
 * Puts transaction in DB and generates unique identifier for it.
 */
public class IdGenerator extends LoggingActor {
    public static final Long RANGE = 50L;

    private Map<CreateIdRequest, ActorRef> adjournedRequests = new HashMap<>();
    private Bounds bounds = new Bounds(0L, 0L);
    private boolean messagesRequested = false;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CreateIdRequest.class, m -> processCreateIdRequest(m, sender()))
                .match(NewIdsMessage.class, this::processNewIdsMessage)
                .matchAny(this::unhandled).build();
    }

    private void processNewIdsMessage(NewIdsMessage message) {
        log.info("Process NewIdsMessage: " + message.toString());

        bounds = message.getBounds();
        messagesRequested = false;

        processAdjournedRequests();
    }

    private void processAdjournedRequests() {
        adjournedRequests.entrySet()
                .forEach(e -> processCreateIdRequest(e.getKey(), e.getValue()));

        adjournedRequests = new HashMap<>();
    }

    private boolean processCreateIdRequest(CreateIdRequest message, ActorRef receiver) {
        log.info("Process CreateIdRequest: " + message.toString());

        if(outOfIndexes()) {
            log.info("Out of indexes!");

            adjournedRequests.put(message, receiver);
            if(!messagesRequested) {
                requestBounds();
            }

            return false;
        } else {
            Bounds bounds = getIndexes();
            Transaction transaction = builder()
                    .betInfo(message.getBetInfo())
                    .state(State.CREATED)
                    .lower(bounds.getLowerBound())
                    .upper(bounds.getUpperBound())
                    .build();

            log.info("Saving new transaction: " + transaction);

            DBConnection.newTransaction(transaction,
                tr -> receiver.tell(new CreateIdResponse(transaction), self()));

            return true;
        }
    }

    private void requestBounds() {
        DBConnection.createIdentifiers(
                bounds -> self().tell(new NewIdsMessage(bounds), self()));
        messagesRequested = true;

        log.info("Bounds requested");
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
