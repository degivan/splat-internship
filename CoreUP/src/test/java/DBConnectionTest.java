import org.junit.Test;
import ru.splat.db.DBConnection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Иван on 03.02.2017.
 */
public class DBConnectionTest {
    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void testFindUnfinishedTransactions() throws InterruptedException {
        DBConnection.processUnfinishedTransactions(transactions ->
                transactions.forEach(transaction ->
                        System.out.println(transaction.toString())), () -> {});
        lock.await(2000, TimeUnit.MILLISECONDS);
    }
}
