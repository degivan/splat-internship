package ru.splat.main;

import com.google.protobuf.Message;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.BetOutcome;
import ru.splat.messages.uptm.trmetadata.BetTask;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.protobuf.ProtobufFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Дмитрий on 03.02.2017.
 */
//после пулла перестал работать JUnit
public class Test {
    public static void main(String[] argv) throws Exception {
        testProtobuf();
    }
    static void testProtobuf() throws Exception {
        List<ServicesEnum> services = new LinkedList<>();
        services.add(ServicesEnum.BetService);
        List<BetOutcome> betOutcomes = new LinkedList<>();
        //BetOutcome bo = new BetOutcome(1L, 2L, 3.14);
        betOutcomes.add(new BetOutcome(1L, 2L, 3.14));
        LocalTask bet1 = new BetTask(TaskTypesEnum.ADD_BET, 1, betOutcomes);
        //buidling protobuf message
        Message message = ProtobufFactory.buildProtobuf(bet1, services);
        System.out.println("Protobuf is builtttt");
        //LocalTask event = new EventTask(TaskTypesEnum.CHECK_EVENT_LIMIT);
    }
}
