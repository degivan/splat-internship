//import org.junit.Test;
//import ru.splat.actors.IdGenerator;
//import ru.splat.actors.RegistryActor;
//import ru.splat.db.Bounds;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Created by Иван on 14.02.2017.
// */
//public class RegistryActorTest {
//    @Test
//    public void testBoundsFromTrId() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Bounds b = new Bounds(0L, IdGenerator.RANGE);
//        Method method = RegistryActor.class.getDeclaredMethod("boundsFromTrId", Long.class);
//        method.setAccessible(true);
//
//        Bounds testB = (Bounds) method.invoke(null, 0L);
//        Bounds testB2 = (Bounds) method.invoke(null, 1L);
//
//        assertEquals(b, testB);
//        assertEquals(b, testB2);
//    }
//}
