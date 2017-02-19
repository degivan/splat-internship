import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.splat.actors.PhaserActor;
import ru.splat.messages.uptm.trstate.TransactionState;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Иван on 19.02.2017.
 */
public class PhaserActorTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    //Results can be viewed in testLog
    public void testInitialState() {
        new JavaTestKit(system) {{
            ActorRef phaser = system.actorOf(Props.create(PhaserActor.class, ActorRef.noSender(), getRef())
                    .withDispatcher("phaser-dispatcher"), "test-phaser");
            phaser.tell(new TransactionState(), getRef());
            expectNoMsg(Duration.apply(20L, TimeUnit.SECONDS));
        }};
    }
}
