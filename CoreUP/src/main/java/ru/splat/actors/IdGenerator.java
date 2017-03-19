package ru.splat.actors;

import akka.actor.ActorRef;
import akka.japi.Pair;
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
public class IdGenerator extends LoggingActor {
    static final Long RANGE = 50L;

    private Queue<Pair<CreateIdRequest, ActorRef>> adjournedRequests = new LinkedList<>();
    private Bounds bounds = new Bounds(0L, 0L);
    private boolean messagesRequested = false;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(CreateIdRequest.class, m -> {
                     log.info("CreateIdRequest delivered: " + m.toString());
                     processCreateIdRequest(m, sender());
                })
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
        while(adjournedRequests.peek() != null) {
            Pair<CreateIdRequest, ActorRef> pair = adjournedRequests.poll();
            if(!processCreateIdRequest(pair.first(), pair.second())) {
                break;
            }
        }
    }

    private boolean processCreateIdRequest(CreateIdRequest message, ActorRef receiver) {
        if(outOfIndexes()) {
            adjournedRequests.add(new Pair<>(message, receiver));
            if(!messagesRequested) {
                log.info("Out of indexes!");

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
                    .freshBuild();

            log.info("Saving new transaction: " + transaction);

            DBConnection.newTransaction(transaction,
                tr -> receiver.tell(new CreateIdResponse(transaction), self()),
                executor);

            return true;
        }
    }

    private void requestBounds() {
        DBConnection.createIdentifiers(
                bounds -> self().tell(new NewIdsMessage(bounds), self())
        );
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
