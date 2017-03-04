import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import junit.framework.TestCase;
import org.slf4j.Logger;
import ru.splat.messages.BetRequest;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trmetadata.*;
import ru.splat.messages.uptm.trmetadata.bet.AddBetTask;
import ru.splat.messages.uptm.trmetadata.bet.BetOutcome;
import ru.splat.messages.uptm.trmetadata.bet.FixBetTask;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.tm.LoggerGlobal;
import ru.splat.tm.actors.*;
import ru.splat.tm.mocks.MockRegistry;
import ru.splat.tm.messages.PollMsg;
import ru.splat.tm.protobuf.ProtobufFactory;
import ru.splat.tm.protobuf.ResponseParser;
import scala.concurrent.duration.Duration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by Дмитрий on 01.01.2017.
 */

public class TMTest extends TestCase {
    private Set<ServicesEnum> services;
    private Set<Integer> servicesOrd;
    private ResponseParser responseParser;
    private final Logger LOGGER = getLogger(TMActor.class);

    public void testActors() {

    }

    public void testBetProtobufP1() throws Exception {
        Set<BetOutcome> betOutcomes = new HashSet<>();
        betOutcomes.add(new BetOutcome(1, 2, 1, 3.14));
        LocalTask bet1 = new AddBetTask(1, betOutcomes, System.currentTimeMillis());
        BetRequest.Bet betMessage = (BetRequest.Bet) ProtobufFactory.buildProtobuf(bet1, services);
        assertEquals(betMessage.getPunterId(), 1);
        Set<Integer> servicesOut = new HashSet<Integer>(betMessage.getServicesList());
        assertEquals(servicesOut, servicesOrd);
    }

    public void testBetProtobufP2() throws Exception{
        LocalTask bet1 = new FixBetTask(1L, System.currentTimeMillis());
        BetRequest.Bet betMessage = (BetRequest.Bet) ProtobufFactory.buildProtobuf(bet1, services);
        assertEquals(betMessage.getId(), 1L);
        assertTrue(betMessage.getBetOutcomeList().isEmpty());
    }
    //проверить после получения от кафки
    public void testResponseParser() {
        Response.ServiceResponse message = Response.ServiceResponse.newBuilder().addAllServices(servicesOrd)
               .setBooleanAttachment(true).setResult(1).build();
        ServiceResponse serviceResponse = ResponseParser.unpackMessage(message);
        assertTrue(message != null);
        LoggerGlobal.log(serviceResponse.getAttachment().toString());
        assertEquals(serviceResponse.getAttachment(), true);
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        services = new HashSet<>();
        services.add(ServicesEnum.BetService);
        services.add(ServicesEnum.EventService);
        services.add(ServicesEnum.BillingService);
        services.add(ServicesEnum.PunterService);
        servicesOrd = services.stream().map(Enum::ordinal)
                .collect(Collectors.toSet());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public TMTest() {

    }
}
