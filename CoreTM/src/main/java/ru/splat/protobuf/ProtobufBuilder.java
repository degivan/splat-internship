package ru.splat.protobuf;

import com.google.protobuf.Message;
import ru.splat.protobuf.BetReqProto;
import ru.splat.protobuf.BillingReqProto;
import ru.splat.protobuf.EventReqProto;
import ru.splat.trmetadata.*;

import java.util.List;

/**
 * Created by Дмитрий on 17.01.2017.
 */
//класс для формирования сообщений
    //TO-DO: необходимо протестировать возможность добавления пустых полей
public  class ProtobufBuilder {
    public ProtobufBuilder() {
    }
    //подготовить мессаги для оптравки
    //надо придумать, что делать с enum-и
    public static Message buildProtobuf(Long transactionId, LocalTask localTask, List<String> taskList) throws Exception{
        Message message;
        if (localTask instanceof BetTask) {

            Message.Builder builder = BetReqProto.BetReq.newBuilder()
                    .setTransactionId(transactionId)
                    .setLocalTask(localTask.getType().toString())
                    .setBetState(((BetTask) localTask).getBetState().toString())
                    .setPunterId(((BetTask) localTask).getPunterId())
                    .addAllTasks(taskList);
            if (((BetTask) localTask).getBetState() != null) {
            }
            message =  builder.build();
            return message;
        }
        if (localTask instanceof BillingTask) {
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
        }
        else {
            throw new Exception("Unknown task type!");
        }
    }
}
