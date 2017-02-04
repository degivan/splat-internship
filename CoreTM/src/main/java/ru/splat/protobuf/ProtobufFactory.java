package ru.splat.protobuf;

import com.google.protobuf.Message;
import ru.splat.messages.BetRequest;
import ru.splat.messages.BillingRequest;
import ru.splat.messages.EventRequest;
import ru.splat.messages.PunterRequest;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trmetadata.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Дмитрий on 17.01.2017.
 */
//класс для формирования сообщений
    //TO-DO: необходимо протестировать возможность добавления пустых полей
public  class ProtobufFactory {
    public ProtobufFactory() {
    }
    //подготовить мессаги для оптравки
    public static Message buildProtobuf(LocalTask localTask, Set<ServicesEnum> _services) throws Exception{

        if (localTask instanceof BetTask) {
            BetRequest.Bet message;
            List<Integer> services = _services.stream().map(servicesEnum -> servicesEnum.ordinal())
                    .collect(Collectors.toList());
            BetRequest.Bet.Builder builder = BetRequest.Bet.newBuilder()
                    .setLocalTask(localTask.getType().ordinal())
                    .setPunterId(((BetTask)localTask).getPunterId())
                    .addAllBetOutcome(((BetTask)localTask).getBetOutcomes())
                    .addAllServices(services);
            message =  builder.build();

            /*Message.Builder builder = BetRequest.Bet.newBuilder()
                    .setTransactionId(transactionId)
                    .setLocalTask(localTask.getType().toString())
                    .setBetState(((BetTask) localTask).getBetState().toString())
                    .setPunterId(((BetTask) localTask).getPunterId())
                    .addAllTasks(taskList);
            if (((BetTask) localTask).getBetState() != null) {
            }*/

            return message;
        }
        /*if (localTask instanceof BillingTask) {
            Message.Builder builder = BillingReqProto.BillingReq.
                    newBuilder()
                    .setTransactionId(transactionId)
                    .setLocalTask(localTask.getType().toString())
                    .setPunterId(((BillingTask) localTask).getPunterId())
                    .setSum(((BillingTask) localTask).getSum())
                    .addAllTasks(taskList);

            message = builder.build();
            return message;
        }
        if (localTask instanceof EventTask) {
            int i = 0;
            Message.Builder builder = EventReqProto.EventReq.
                    newBuilder()
                    .setTransactionId(transactionId)
                    .setLocalTask(localTask.getType().toString())
                    .setPunterId(((EventTask) localTask).getPunterId())
                    .addAllTasks(taskList)
                    .addAllEvents(((EventTask) localTask).getEventIdList())
                    .addAllSelections(((EventTask) localTask).getSelectionIdList());



            message = builder.build();
            return message;
        }
        if (localTask instanceof PunterTask) {
            Message.Builder builder =  EventReqProto.EventReq.newBuilder()
                    .setTransactionId(transactionId)
                    .setLocalTask(localTask.getType().toString())
                    .setPunterId(((EventTask) localTask).getPunterId())
                    .addAllTasks(taskList);;

            message = builder.build();
            return message;
        }*/
        else {
            throw new Exception("Unknown task type!");
        }
    }
}
