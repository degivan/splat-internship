//import akka.actor.ActorSystem;
//import akka.event.Logging;
//import akka.event.LoggingAdapter;
//import junit.framework.TestCase;
//import org.apache.kafka.common.TopicPartition;
//import ru.splat.tm.util.TopicTracker;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//import java.util.stream.LongStream;
//
///**
// * Created by Дмитрий on 01.03.2017.
// */
//public class TopicTrackerTest extends TestCase {
//
//    /* в случае, когда все транзакции, записи о которых есть в топиках, закоммичены,
//        все топик трекеры должны вернуть currentOffset равный (начальный currentOffset + n записей в топике)
//     */
//    public void testAllCommited() {
//        ActorSystem system = ActorSystem.create("topic_tracker_test");
//        long finalOffset = 100;
//        LoggingAdapter log = Logging.getLogger(system, TopicTracker.class);
//        TopicTracker tracker_1 = new TopicTracker(new TopicPartition("BillingRes", 0), 0, log);
//        TopicTracker tracker_2 = new TopicTracker(new TopicPartition("BillingRes", 0), 0, log);
//        TopicTracker tracker_3 = new TopicTracker(new TopicPartition("BillingRes", 0), 0, log);
//        ArrayList<Long> trIds = new ArrayList<>(); for (long trId = 0; trId < finalOffset; trId++) trIds.add(trId); //список всех транзакций
//        Collections.shuffle(trIds);
//        for (int i = 0; i < trIds.size(); i++) tracker_1.addRecord(i, trIds.get(i));
//        Collections.shuffle(trIds);
//        for (int i = 0; i < trIds.size(); i++) tracker_2.addRecord(i, trIds.get(i));
//        Collections.shuffle(trIds);
//        for (int i = 0; i < trIds.size(); i++) tracker_3.addRecord(i, trIds.get(i));
//        trIds.forEach(trId -> {tracker_1.commitTransaction(trId); tracker_2.commitTransaction(trId); tracker_3.commitTransaction(trId);});
//        assertEquals(tracker_1.getCurrentOffset(), finalOffset);
//        assertEquals(tracker_2.getCurrentOffset(), finalOffset);
//        assertEquals(tracker_3.getCurrentOffset(), finalOffset);
//    }
//    /* в случае, когда транзакция, записанная в TopicTracker-х под offset-ми i не была закоммичена, а все предыдущие были,
//        все топик трекеры должны вернуть currentOffset равный соовтетствующим i
//     */
//    public void testBlockingTransaction() {
//        ActorSystem system = ActorSystem.create("topic_tracker_test");
//        final long upperBound = 100; final long lowerBound = 1; final int quantity = 100;
//        LoggingAdapter log = Logging.getLogger(system, TopicTracker.class);
//        TopicTracker tracker_1 = new TopicTracker(new TopicPartition("BillingRes", 0), 0, log);
//        TopicTracker tracker_2 = new TopicTracker(new TopicPartition("BillingRes", 0), 0, log);
//        TopicTracker tracker_3 = new TopicTracker(new TopicPartition("BillingRes", 0), 0, log);
//        List<Long> trIds = new Random().longs(quantity, lowerBound, upperBound).boxed().collect(Collectors.toList());
//        Random randomizer = new Random();
//        long block_1 = upperBound + 1; int offset_1_1 = randomizer.nextInt(quantity / 2); int offset_1_2 = randomizer.nextInt(quantity / 2); int offset_1_3 = randomizer.nextInt(quantity / 2);
//        System.out.println("offset_1_1: " + offset_1_1 + ", offset_1_2: " + offset_1_2 + ", offset_1_3: " + offset_1_3);
//        long block_2 = upperBound + 2; int offset_2_1 = quantity / 2 + randomizer.nextInt(quantity / 2);
//        int offset_2_2 = quantity / 2 + randomizer.nextInt(quantity / 2);
//        int offset_2_3 = quantity / 2 + randomizer.nextInt(quantity / 2);
//        System.out.println("offset_2_1: " + offset_2_1 + ", offset_2_2: " + offset_2_2 + ", offset_2_3: " + offset_2_3);
//        ArrayList<Long> trIds_1 = new ArrayList<>(trIds); ArrayList<Long> trIds_2 = new ArrayList<>(trIds); ArrayList<Long> trIds_3 = new ArrayList<>(trIds);
//        Collections.shuffle(trIds_1);
//        trIds_1.add(offset_1_1, block_1); trIds_1.add(offset_2_1, block_2);
//        for (int i = 0; i < trIds_1.size(); i++) tracker_1.addRecord(i, trIds_1.get(i));
//        Collections.shuffle(trIds_2);
//        trIds_2.add(offset_1_2, block_1); trIds_2.add(offset_2_2, block_2);
//        for (int i = 0; i < trIds_2.size(); i++) tracker_2.addRecord(i, trIds_2.get(i));
//        Collections.shuffle(trIds_3);
//        trIds_3.add(offset_1_3, block_1); trIds_3.add(offset_2_3, block_2);
//        for (int i = 0; i < trIds_3.size(); i++) tracker_3.addRecord(i, trIds_3.get(i));
//        //коммитим все транзакции, кроме block_1
//        trIds.forEach(trId -> {tracker_1.commitTransaction(trId); tracker_2.commitTransaction(trId); tracker_3.commitTransaction(trId);});
//        assertEquals(offset_1_1, tracker_1.getCurrentOffset());
//        assertEquals(offset_1_2, tracker_2.getCurrentOffset());
//        assertEquals(offset_1_3, tracker_3.getCurrentOffset());
//        //коммитим транзакцию, которая не была добавлена. Ничего не должно измениться
//        tracker_1.commitTransaction(upperBound + 100); tracker_2.commitTransaction(upperBound + 100); tracker_3.commitTransaction(upperBound + 100);
//        assertEquals(offset_1_1, tracker_1.getCurrentOffset());
//        assertEquals(offset_1_2, tracker_2.getCurrentOffset());
//        assertEquals(offset_1_3, tracker_3.getCurrentOffset());
//        //коммититм block_1, трекеры должны остановиться на оффсетах block_2
//        tracker_1.commitTransaction(block_1); tracker_2.commitTransaction(block_1); tracker_3.commitTransaction(block_1);
//        assertEquals(offset_2_1, tracker_1.getCurrentOffset());
//        assertEquals(offset_2_2, tracker_2.getCurrentOffset());
//        assertEquals(offset_2_3, tracker_3.getCurrentOffset());
//        //коммитим block_2 трекеры должны остановиться на оффсетах quantity + 2
//        tracker_1.commitTransaction(block_2); tracker_2.commitTransaction(block_2); tracker_3.commitTransaction(block_2);
//        assertEquals(quantity + 2, tracker_1.getCurrentOffset());
//        assertEquals(quantity + 2, tracker_2.getCurrentOffset());
//        assertEquals(quantity + 2, tracker_3.getCurrentOffset());
//        system.terminate();
//
//    }
//
//    //public void testAl
//}
