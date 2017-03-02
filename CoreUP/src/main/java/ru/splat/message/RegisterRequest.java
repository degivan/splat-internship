package ru.splat.message;

import akka.actor.ActorRef;
import ru.splat.db.Bounds;

/**
 * Message from receiver to registry.
 */
public class RegisterRequest implements InnerMessage {
    private final Bounds bounds;
    private final ActorRef actor;

    public RegisterRequest(Bounds bounds, ActorRef actor) {
        this.bounds = bounds;
        this.actor = actor;
    }

    public ActorRef getActor() {
        return actor;
    }

    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "bounds=" + bounds +
                ", actor=" + actor +
                '}';
    }
}
