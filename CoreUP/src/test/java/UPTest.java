import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.splat.UP;
import ru.splat.messages.proxyup.ProxyUPMessage;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.proxyup.bet.NewRequest;
import ru.splat.messages.proxyup.bet.NewResponse;

import java.util.HashSet;

/**
 * Created by Иван on 18.12.2016.
 */
public class UPTest {
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
    public void test1() throws InterruptedException {
       new JavaTestKit(system) {{
           UP up = UP.create();
           up.start();
           for(int i = 0; i < 1; i++) {
               up.getReceiver(i).tell(testRequest(i), getRef());
           }
           final NewResponse[] out =
                   new ReceiveWhile<NewResponse>(
                           NewResponse.class,
                           duration("40 seconds"),
                           duration("40 seconds"),
                           30
                           ) {

                       @Override
                       protected NewResponse match(Object msg) throws Exception {
                           return null;
                       }
                   }.get();
       }};
    }

    private ProxyUPMessage testRequest(Integer userId) {
        BetInfo requestInfo = new BetInfo();
        requestInfo.setUserId(userId);
        requestInfo.setBet(2);
        requestInfo.setSelectionsId(new HashSet<>());
        requestInfo.setBetOutcomes(new HashSet<>());
        return new NewRequest(requestInfo);
    }
}
