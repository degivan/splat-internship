/**
 * Created by Дмитрий on 15.02.2017.
 */

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.Test;

public class DispatcherTest {

    @Test
    public void createWithDispatcherTest() {
        ActorSystem system = ActorSystem.create();
        ActorRef mockRegistry = system.actorOf(Props.create(ru.splat.tm.messages.MockRegistry.class).withDispatcher("phaser-dispatcher"),
        "mock-registry");
        ActorRef tmActor = system.actorOf(Props.create(ru.splat.tm.actors.TMActor.class, mockRegistry).withDispatcher("tm-actor-dispatcher"),
                "tmactor");
    }
}
