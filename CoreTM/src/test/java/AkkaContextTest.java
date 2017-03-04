///**
// * Created by Дмитрий on 15.02.2017.
// */
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import junit.framework.TestCase;
//import org.junit.Test;
//import ru.splat.tm.mocks.MockRegistry;
//
//public class AkkaContextTest extends TestCase {
//
//    @Test
//    public void createWithDispatcherTest() {
//        ActorSystem system = ActorSystem.create();
//        ActorRef mockRegistry = system.actorOf(Props.create(MockRegistry.class).withDispatcher("my-settings.akka.actor.phaser-dispatcher"),
//                "mock-registry");
//    }
//}
