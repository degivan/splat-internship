//import com.mongodb.async.client.MongoClients;
//import org.junit.Test;
//import ru.splat.db.DBConnection;
//import ru.splat.messages.conventions.ServiceResult;
//import ru.splat.messages.uptm.trstate.ServiceResponse;
//import ru.splat.messages.uptm.trstate.TransactionState;
//
//import java.util.HashMap;
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
//    public void testFindUnfinishedTransactions() throws InterruptedException {
//        DBConnection.processUnfinishedTransactions(transactions ->
//                transactions.forEach(transaction ->
//                        System.out.println(transaction.toString())), () -> {});
//        lock.await(2000, TimeUnit.MILLISECONDS);
//    }
//
//    @Test
//    public void testConnectionsSize() {
//        System.err.println(MongoClients.create()
//                .getSettings()
//                .getConnectionPoolSettings()
//                .getMaxSize());
//    }

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
