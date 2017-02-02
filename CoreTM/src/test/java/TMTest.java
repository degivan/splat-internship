//import ru.splat.messages


import org.junit.Test;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.*;
import ru.splat.messages.uptm.trstate.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Дмитрий on 01.01.2017.
 */

public class TMTest {
    @Test
    public void testProtobuf() {
        List<BetOutcome> betOutcomes = new LinkedList<>();
        //BetOutcome bo = new BetOutcome(1L, 2L, 3.14);
        betOutcomes.add(new BetOutcome(1L, 2L, 3.14));
        LocalTask bet = new BetTask(TaskTypesEnum.ADD_BET, 1L, betOutcomes);
        //LocalTask event = new EventTask(TaskTypesEnum.CHECK_EVENT_LIMIT);
    }

}
