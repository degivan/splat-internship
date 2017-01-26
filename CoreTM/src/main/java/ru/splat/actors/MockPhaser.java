package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ru.splat.conventions.TaskTypesEnum;
import ru.splat.trmetadata.LocalTask;
import ru.splat.trmetadata.PunterTask;
import ru.splat.trmetadata.TransactionMetadata;

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
            PunterTask pt = new PunterTask(TaskTypesEnum.CHECK_PUNTER_LIMITS, 10 - trid);
            TransactionMetadata trMet = new TransactionMetadata(trid, taskList);
            tmActor.tell(trMet, getSelf());
        }

    }
}
