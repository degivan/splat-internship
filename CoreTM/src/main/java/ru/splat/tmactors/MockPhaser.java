package ru.splat.tmactors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ru.splat.messages.conventions.TaskTypesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.punter.AddPunterLimitsTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Дмитрий on 17.01.2017.
 */
//Заглушка для тестирования ТМ
    //Принимает Id транзакции
public class MockPhaser extends UntypedActor {
    private ActorRef tmActor;

    public MockPhaser(ActorRef tmActor) {
        this.tmActor = tmActor;
    }

    public void onReceive(Object o) throws Exception {
        if (o instanceof Long) {
            Long trid = (Long) o;
           // ActorRef tmActor = getContext().actorFor("tmActor");
            List<LocalTask> taskList = new LinkedList<LocalTask>();
            AddPunterLimitsTask pt = new AddPunterLimitsTask(TaskTypesEnum.ADD_PUNTER_LIMITS, 10 - trid, System.currentTimeMillis());
            TransactionMetadata trMet = new TransactionMetadata(trid, taskList);
            tmActor.tell(trMet, getSelf());
        }

    }
}
