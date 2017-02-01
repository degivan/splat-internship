package ru.splat;

import akka.actor.ActorRef;

/**
 * Placeholder for TM1.
 */
public class TM1 {
    private final ActorRef tmActor;

    public TM1(ActorRef tmActor) {
        this.tmActor = tmActor;
    }

    public static TM1 create(ActorRef tmActor) {
        return new TM1(tmActor);
    }

    public ActorRef getTmActor() {
        return tmActor;
    }
}
