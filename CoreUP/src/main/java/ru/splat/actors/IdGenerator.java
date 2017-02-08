package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ru.splat.db.Bounds;
import ru.splat.db.DBConnection;
import ru.splat.message.CreateIdRequest;
import ru.splat.message.CreateIdResponse;
import ru.splat.message.NewIdsMessage;
import ru.splat.messages.Transaction.State;

import java.util.LinkedList;
import java.util.Queue;

import static ru.splat.messages.Transaction.Builder.*;

/**
 * Puts transaction in DB and generates unique identifier for it.
 */
public class IdGenerator extends UntypedActor {
    private static final Long RANGE = 50L;

    private Queue<CreateIdRequest> adjournedRequests = new LinkedList<>();
    private Bounds bounds = new Bounds(0L, 0L);
    private boolean messagesRequested = false;

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof CreateIdRequest) {
            processCreateIdRequest((CreateIdRequest) message);
        } else if(message instanceof NewIdsMessage) {
            processNewIdsMessage((NewIdsMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void processNewIdsMessage(NewIdsMessage message) {
        bounds = message.getBounds();
        messagesRequested = false;

        while(adjournedRequests.peek() != null &&
              processCreateIdRequest(adjournedRequests.poll())) {}
    }

    private boolean processCreateIdRequest(CreateIdRequest message) {
        if(outOfIndexes()) {
            adjournedRequests.add(message);
            if(!messagesRequested) {
                DBConnection.createIdentifiers(
                        bounds -> getSelf().tell(new NewIdsMessage(bounds), getSelf()));
                messagesRequested = true;
            }
            return false;
        } else {
            ActorRef receiver = getSender();
            Bounds bounds = getIndexes();

            DBConnection.newTransaction(
                builder()
                    .betInfo(message.getBetInfo())
                    .state(State.CREATED)
                    .lower(bounds.getLowerBound())
                    .upper(bounds.getUpperBound())
                    .build(),
                transaction -> receiver.tell(new CreateIdResponse(transaction), getSelf()));

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
