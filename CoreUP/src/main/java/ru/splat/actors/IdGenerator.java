package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ru.splat.message.CreateIdResponse;
import ru.splat.db.DBConnection;
import ru.splat.message.CreateIdRequest;

/**
 * Puts transaction in DB and generates unique identifier for it.
 */
public class IdGenerator extends UntypedActor {
    //TODO: generate range of possible identifiers and then use them instead of querying database every time
    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof CreateIdRequest) {
            CreateIdRequest request = (CreateIdRequest) message;
            ActorRef receiver = getSender();
            DBConnection.newTransaction(request.getBetInfo(),
                    transaction -> receiver.tell(new CreateIdResponse(transaction), getSelf()));
        }
    }
}
