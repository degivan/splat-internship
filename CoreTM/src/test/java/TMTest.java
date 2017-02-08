//import ru.splat.messages


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import com.google.protobuf.Message;
import junit.framework.TestCase;
import ru.splat.messages.BetRequest;
import ru.splat.messages.Response;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.*;
import ru.splat.messages.uptm.trmetadata.bet.AddBetTask;
import ru.splat.messages.uptm.trmetadata.bet.BetOutcome;
import ru.splat.messages.uptm.trmetadata.bet.FixBetTask;
import ru.splat.messages.uptm.trmetadata.punter.AddPunterLimitsTask;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.mocks.BetServiceMock;
import ru.splat.tm.AbsActor;
import ru.splat.tm.TMConsumerActor;
import ru.splat.tm.TMStarter;
import ru.splat.tm.TMStarterImpl;
import ru.splat.tmactors.PollMessage;
import ru.splat.tmactors.TaskSent;
import ru.splat.tmkafka.TMConsumer;
import ru.splat.tmkafka.TMConsumerImpl;
import ru.splat.tmprotobuf.ResponseParser;
import ru.splat.tmprotobuf.ResponseParserImpl;
import ru.splat.tmprotobuf.ProtobufFactory;
import ru.splat.tmprotobuf.ProtobufFactoryImpl;
import scala.concurrent.duration.Duration;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Дмитрий on 01.01.2017.
 */

public class TMTest extends TestCase {
    private Set<ServicesEnum> services;
    private Set<Integer> servicesOrd;
    private ProtobufFactory protobufFactory;
    private ResponseParser responseParser;
    //private TMStarter tmStarter;
    private TMConsumerImpl tmConsumer;

    public void testTMConsumerActor() {
        ActorSystem system = ActorSystem.create("testactors");
        final ActorRef consumerActor = system.actorOf(Props.create(TMConsumerActor.class), "TMConsumerActor");
        Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
                Duration.create(200, TimeUnit.MILLISECONDS), consumerActor, new PollMessage(),
                system.dispatcher(), null);
    }



    /*public void testTMConsumer() {  //test consumer and responseParser work
        tmConsumer = new TMConsumerImpl();
        BetServiceMock betServiceMock = new BetServiceMock();

        betServiceMock.sendRoutine();
        int pollCount = tmConsumer.pollRecords().count(); System.out.println("PollCount: " + pollCount);
        assertEquals(pollCount, 0);
    }*/

    //TMStarter
    /*public void testTMStarter() {
        /*try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long time = System.currentTimeMillis();
        LocalTask fixBet1 = new FixBetTask(20L, time);
        LocalTask punterTask1 = new AddPunterLimitsTask(135, time);
        List<LocalTask> tasks = new LinkedList<>(); tasks.add(fixBet1); tasks.add(punterTask1);
        TransactionMetadata transactionMetadata = new TransactionMetadata(111L, tasks);

        tmStarter.processTransaction(transactionMetadata);  //результат у тестового консюмера
    }*/




    //работоспособность ProtobufFactoryImpl
    public void testBetProtobufP1() throws Exception {
        Set<BetOutcome> betOutcomes = new HashSet<>();
        //BetOutcome bo = new BetOutcome(1L, 2L, 3.14);
        betOutcomes.add(new BetOutcome(1, 2, 3.14));
        LocalTask bet1 = new AddBetTask(1, betOutcomes, System.currentTimeMillis());
        //buidling protobuf message
        BetRequest.Bet betMessage = (BetRequest.Bet) protobufFactory.buildProtobuf(bet1, services);
        //check punter id from generated message
        assertEquals(betMessage.getPunterId(), 1);
        Set<Integer> servicesOut = new HashSet<Integer>(betMessage.getServicesList());
        assertEquals(servicesOut, servicesOrd);

    }

    public void testBetProtobufP2() throws Exception{
        //test ProtobufFactoryImpl for second phase
        LocalTask bet1 = new FixBetTask(1L, System.currentTimeMillis());
        BetRequest.Bet betMessage = (BetRequest.Bet) protobufFactory.buildProtobuf(bet1, services);
        assertEquals(betMessage.getId(), 1L);
        assertTrue(betMessage.getBetOutcomeList().isEmpty());
    }
    //проверить после получения от кафки
    public void testResponseParser() {
        Message message = Response.ServiceResponse.newBuilder().addAllServices(servicesOrd)
               .setBooleanAttachment(true).setResult(1).build();
        ServiceResponse serviceResponse = responseParser.unpackMessage(message);
        assertTrue(message instanceof Response.ServiceResponse);
        System.out.println(serviceResponse.getAttachment());
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

        protobufFactory = new ProtobufFactoryImpl();
        responseParser = new ResponseParserImpl();
        //tmStarter = new TMStarterImpl();
        //tmConsumer = new TMConsumerImpl();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public TMTest() {
        //addTestSuite(TMTest.class);
    }
}
