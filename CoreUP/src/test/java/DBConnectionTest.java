//import akka.actor.ActorSystem;
//import akka.testkit.JavaTestKit;
//import org.junit.Test;
//import ru.splat.db.DBConnection;
//import ru.splat.messages.Transaction;
//import ru.splat.messages.conventions.ServiceResult;
//import ru.splat.messages.proxyup.bet.BetInfo;
//import ru.splat.messages.uptm.trstate.ServiceResponse;
//import ru.splat.messages.uptm.trstate.TransactionState;
//import scala.concurrent.duration.Duration;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static ru.splat.messages.conventions.ServicesEnum.*;
//
///**
// * Created by Иван on 03.02.2017.
// */
//public class DBConnectionTest {
//    private CountDownLatch lock = new CountDownLatch(1);
//
//    @Test
//    public void testClearFinishedTransactions() throws InterruptedException {
//        DBConnection.clearFinishedTransactionsAndStates();
//        lock.await(2000, TimeUnit.MILLISECONDS);
//    }
//
//    @Test
//    public void testNewTransaction() {
//        new JavaTestKit(ActorSystem.create()) {{
//            DBConnection.newTransaction(testTransaction(1),
//                    transaction -> System.err.println("IM HERE"),
//                    getSystem().dispatcher());
//            expectNoMsg(Duration.apply(10L, TimeUnit.SECONDS));
//        }};
//    }
//
//    @Test
//    public void testFindUnfinishedTransactions() throws InterruptedException {
//        DBConnection.processUnfinishedTransactions(transactions ->
//                transactions.forEach(transaction ->
//                        System.out.println(transaction.toString())), () -> {});
//        lock.await(2000, TimeUnit.MILLISECONDS);
//    }
//
//    @Test
//    public void testAddTransactionState() throws InterruptedException {
//        DBConnection.addTransactionState(testState(),
//                transactionState -> System.err.println("I'm here"));
//        lock.await(2000, TimeUnit.MILLISECONDS);
//    }
//
//    @Test
//    public void testFindTransactionState() throws InterruptedException {
//        testAddTransactionState();
//
//        DBConnection.findTransactionState(testState().getTransactionId(),
//                transactionState -> System.err.println(transactionState.toString()));
//        lock.await(2000, TimeUnit.MILLISECONDS);
//    }

//    private static Transaction testTransaction(Integer userId) {
//        BetInfo betInfo = new BetInfo();
//        betInfo.setUserId(userId);
//        betInfo.setBet(2);
//        betInfo.setSelectionsId(new HashSet<>());
//        betInfo.setBetOutcomes(new HashSet<>());
//        return Transaction.Builder.builder()
//                .betInfo(betInfo)
//                .state(Transaction.State.COMPLETED)
//                .lower(0L)
//                .upper(50L)
//                .freshBuild();
//    }
//
//    private static TransactionState testState() {
//        TransactionState trState = new TransactionState(0L, new HashMap<>());
//        trState.setLocalState(BetService, positive());
//        trState.setLocalState(BillingService, positive());
//        trState.setLocalState(EventService, negative());
//        trState.setLocalState(PunterService, negative());
//
//        return trState;
//    }
//
//    private static ServiceResponse<String> positive() {
//        return new ServiceResponse<>("test", ServiceResult.CONFIRMED);
//    }
//
//    private static ServiceResponse<String> negative() {
//        return new ServiceResponse<>("test", ServiceResult.DENIED);
//    }
//}