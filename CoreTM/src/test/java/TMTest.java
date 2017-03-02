////import ru.splat.messages
//
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Cancellable;
//import akka.actor.Props;
//import junit.framework.TestCase;
//import org.slf4j.Logger;
//import ru.splat.messages.BetRequest;
//import ru.splat.messages.Response;
//import ru.splat.messages.conventions.ServicesEnum;
//import ru.splat.messages.uptm.trmetadata.*;
//import ru.splat.messages.uptm.trmetadata.bet.AddBetTask;
//import ru.splat.messages.uptm.trmetadata.bet.BetOutcome;
//import ru.splat.messages.uptm.trmetadata.bet.FixBetTask;
//import ru.splat.messages.uptm.trstate.ServiceResponse;
//import ru.splat.tm.LoggerGlobal;
//import ru.splat.tm.actors.*;
//import ru.splat.tm.mocks.MockRegistry;
//import ru.splat.tm.messages.PollMsg;
//import ru.splat.tm.protobuf.ProtobufFactory;
//import ru.splat.tm.protobuf.ResponseParser;
//import scala.concurrent.duration.Duration;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import static org.slf4j.LoggerFactory.getLogger;
//
///**
// * Created by Дмитрий on 01.01.2017.
// */
//
//public class TMTest extends TestCase {
//    private Set<ServicesEnum> services;
//    private Set<Integer> servicesOrd;
//    private ResponseParser responseParser;
//    //private TMStarter tmStarter;
//    private final Logger LOGGER = getLogger(TMActor.class);
//
//    public void testActors() {
//        //in Main
//    }
//
//    public void testTMConsumerActor() {
//        ActorSystem system = ActorSystem.create("testactors");
//        final ActorRef registry = system.actorOf(Props.create(MockRegistry.class), "MockRegistry");
//        final ActorRef tmActor = system.actorOf(Props.create(TMActor.class, registry), "TMActor");
//        final ActorRef consumerActor = system.actorOf(Props.create(TMConsumerActor.class, tmActor), "TMConsumerActor");
//        Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
//                Duration.create(1000, TimeUnit.MILLISECONDS), consumerActor, new PollMsg(),
//                system.dispatcher(), null);
//
//    }
//
//
//    /*public void testTMActorSend() {
//        ActorSystem system = ActorSystem.create("testactors");
//        final ActorRef tmActor = system.actorOf(Props.create(TMActor.class), "TMActor");
//        Cancellable cancellable = system.scheduler().schedule(Duration.Zero(),
//                Duration.create(2000, TimeUnit.MILLISECONDS), tmActor, new TaskSentMsg(111L, ServicesEnum.BetService),
//                system.dispatcher(), null);
//    }
//    public void testTMConsumer() {  //test consumer and responseParser work
//        tmConsumer = new TMConsumerImpl();
//        ServiceMock betServiceMock = new ServiceMock();
//
//        betServiceMock.sendRoutine();
//        int pollCount = tmConsumer.pollRecords().count(); log.info("PollCount: " + pollCount);
//        assertEquals(pollCount, 0);
//    }*/
//
//    //TMStarter
//    /*public void testTMStarter() {
//        /*try {
//            Thread.sleep(4000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Long time = System.currentTimeMillis();
//        LocalTask fixBet1 = new FixBetTask(20L, time);
//        LocalTask punterTask1 = new AddPunterLimitsTask(135, time);
//        List<LocalTask> tasks = new LinkedList<>(); tasks.add(fixBet1); tasks.add(punterTask1);
//        TransactionMetadata transactionMetadata = new TransactionMetadata(111L, tasks);
//
//        tmStarter.processTransaction(transactionMetadata);  //результат у тестового консюмера
//    }*/
//
//
//
//
//    //работоспособность ProtobufFactory
//    public void testBetProtobufP1() throws Exception {
//        Set<BetOutcome> betOutcomes = new HashSet<>();
//        //BetOutcome bo = new BetOutcome(1L, 2L, 3.14);
//        betOutcomes.add(new BetOutcome(1, 2, 1, 3.14));
//        LocalTask bet1 = new AddBetTask(1, betOutcomes, System.currentTimeMillis());
//        //buidling protobuf message
//        BetRequest.Bet betMessage = (BetRequest.Bet) ProtobufFactory.buildProtobuf(bet1, services);
//        //check punter id from generated message
//        assertEquals(betMessage.getPunterId(), 1);
//        Set<Integer> servicesOut = new HashSet<Integer>(betMessage.getServicesList());
//        assertEquals(servicesOut, servicesOrd);
//
//    }
//
//    public void testBetProtobufP2() throws Exception{
//        //test ProtobufFactory for second phase
//        LocalTask bet1 = new FixBetTask(1L, System.currentTimeMillis());
//        BetRequest.Bet betMessage = (BetRequest.Bet) ProtobufFactory.buildProtobuf(bet1, services);
//        assertEquals(betMessage.getId(), 1L);
//        assertTrue(betMessage.getBetOutcomeList().isEmpty());
//    }
//    //проверить после получения от кафки
//    public void testResponseParser() {
//        Response.ServiceResponse message = Response.ServiceResponse.newBuilder().addAllServices(servicesOrd)
//               .setBooleanAttachment(true).setResult(1).build();
//        ServiceResponse serviceResponse = ResponseParser.unpackMessage(message);
//        assertTrue(message != null);
//        LoggerGlobal.log(serviceResponse.getAttachment().toString());
//        assertEquals(serviceResponse.getAttachment(), true);
//    }
//
//
//
//
//    @Override
//    public void setUp() throws Exception {
//        super.setUp();
//
//        services = new HashSet<>();
//        services.add(ServicesEnum.BetService);
//        services.add(ServicesEnum.EventService);
//        services.add(ServicesEnum.BillingService);
//        services.add(ServicesEnum.PunterService);
//        servicesOrd = services.stream().map(Enum::ordinal)
//                .collect(Collectors.toSet());
//
//        //tmStarter = new TMStarterImpl();
//        //tmConsumer = new TMConsumerImpl();
//    }
//
//    @Override
//    public void tearDown() throws Exception {
//        super.tearDown();
//    }
//
//    public TMTest() {
//        //addTestSuite(TMTest.class);
//    }
//}
