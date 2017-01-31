package ru.splat.actors;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import ru.splat.UP;
import ru.ifmo.splat.messages.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.splat.messages.proxyup.ProxyUPMessage;
import ru.ifmo.splat.messages.proxyup.bet.BetInfo;

import java.util.ArrayList;

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
           up.getReceivers().get(0).tell(testRequest(), getRef());
           expectMsgAnyClassOf(Transaction.class);
       }};
    }

    private ProxyUPMessage testRequest() {
        BetInfo requestInfo = new BetInfo();
        requestInfo.setUserId(100L);
        requestInfo.setBet(2L);
        requestInfo.setSelectionsId(new ArrayList<>());
        requestInfo.setEventsId(new ArrayList<>());
        return ProxyUPMessage.of(requestInfo);
    }
}
