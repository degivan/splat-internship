//import ru.splat.messages


import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Test;
import junit.runner.BaseTestRunner;
import org.junit.runner.RunWith;
import ru.splat.messages.BetRequest;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.*;
import ru.splat.messages.uptm.trstate.*;
import ru.splat.protobuf.ProtobufFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Дмитрий on 01.01.2017.
 */

public class TMTest extends TestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public TMTest() {
        //addTestSuite(TMTest.class);
    }

    //работоспособность ProtobufFactory
    public void testProtobuf() throws Exception {
        Set<ServicesEnum> services = new HashSet<>();
        //поля с сервисами и id транзакции содержатся в TransactionMetadata
        services.add(ServicesEnum.BetService);
        services.add(ServicesEnum.EventService);
        services.add(ServicesEnum.BillingService);
        services.add(ServicesEnum.PunterService);

        List<BetOutcome> betOutcomes = new LinkedList<>();
        //BetOutcome bo = new BetOutcome(1L, 2L, 3.14);
        betOutcomes.add(new BetOutcome(1L, 2L, 3.14));
        LocalTask bet1 = new BetTask(TaskTypesEnum.ADD_BET, 1, betOutcomes);
        //buidling protobuf message
        Message betMessage = ProtobufFactory.buildProtobuf(bet1, services);
        //check punter id from generated message
        assertEquals(betMessage.getField(betMessage.getDescriptorForType().findFieldByName("punterId")), 1);
        //List<BetOutcome> servicesOut = betMessage.getField(betMessage.getDescriptorForType().findFieldByName("services"));
        //List<BetOutcome> servicesOut = betMessage.
        //List<Integer> selections = new LinkedList<>(); selections.add(134); selections.add(144);
        //LocalTask event = new EventTask(TaskTypesEnum.ADD_SELECTION_LIMIT, selections);
    }

    public void testProtobufNullFields() {

    }

}
