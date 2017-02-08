package ru.splat.tm;

import akka.actor.AbstractActor;
import ru.splat.tmactors.TaskSent;

/**
 * Created by Дмитрий on 07.02.2017.
 */
public class AbsActor extends AbstractActor{

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskSent.class, m -> notifySent(m))
                .build();
    }

    private void notifySent(TaskSent m) {
        System.out.println("task " + m.getTaskType().toString() + " of " + m.getTransactionId() + " is sent");
    }

}
