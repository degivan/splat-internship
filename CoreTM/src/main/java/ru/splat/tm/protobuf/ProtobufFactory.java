package ru.splat.tm.protobuf;

import com.google.protobuf.Message;
import ru.splat.messages.BetRequest;
import ru.splat.messages.BillingRequest;
import ru.splat.messages.EventRequest;
import ru.splat.messages.PunterRequest;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.bet.AddBetTask;
import ru.splat.messages.uptm.trmetadata.bet.CancelBetTask;
import ru.splat.messages.uptm.trmetadata.bet.FixBetTask;
import ru.splat.messages.uptm.trmetadata.billing.BillingWithdrawTask;
import ru.splat.messages.uptm.trmetadata.billing.CancelWithdrawTask;
import ru.splat.messages.uptm.trmetadata.event.AddSelectionLimitsTask;
import ru.splat.messages.uptm.trmetadata.event.CancelSelectionLimitsTask;
import ru.splat.messages.uptm.trmetadata.punter.AddPunterLimitsTask;
import ru.splat.messages.uptm.trmetadata.punter.CancelPunterLimitsTask;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Дмитрий on 17.01.2017.
 */
//простая фабрика протобуфов
    //TO-DO: необходимо протестировать возможность добавления пустых полей (Optional<>)
    //Избавиться от привязки к классам
public  class ProtobufFactory{
    public ProtobufFactory() {
    }
    //подготовить мессаги для оптравки
    public static Message buildProtobuf(LocalTask localTask, Set<ServicesEnum> _services) throws IllegalArgumentException{
        Set<Integer> services = _services.stream().map(Enum::ordinal)
                .collect(Collectors.toSet());
        Message message;
        //для BetService
        if (localTask instanceof AddBetTask) {
            AddBetTask task = (AddBetTask)localTask;
            //BetRequest.Bet message;
            Set betOutcomes = (task.getBetOutcomes().stream()
                    .map(betOutcome ->  BetRequest.Bet.BetOutcome.newBuilder()
                                .setCoefficient(betOutcome.getCoefficient())
                                .setEventId(betOutcome.getEventId())
                                .setId(betOutcome.getOutcomeId())
                                .build()
                    ).collect(Collectors.toSet()));
            BetRequest.Bet.Builder builder = BetRequest.Bet.newBuilder()
                    .setLocalTask(task.getType().ordinal())
                    .setPunterId(task.getPunterId())
                    .setTime(localTask.getTime())
                    //.setId(((AddBetTask)localTask).getBetId())
                    .addAllBetOutcome(betOutcomes)   //неверно
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        if (localTask instanceof FixBetTask) {
            FixBetTask task = (FixBetTask)localTask;
            //BetRequest.Bet message;
            BetRequest.Bet.Builder builder = BetRequest.Bet.newBuilder()
                    .setLocalTask(task.getType().ordinal())
                    .setId(task.getBetId())
                    .setTime(localTask.getTime())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        //BillingService tasks
        if (localTask instanceof BillingWithdrawTask) {
            BillingWithdrawTask task = (BillingWithdrawTask)localTask;
            Message.Builder builder = BillingRequest.Billing.
                    newBuilder()
                    .setLocalTask(localTask.getType().ordinal())
                    .setPunterId(task.getPunterId())
                    .setTime(localTask.getTime())
                    .setSum(task.getSum())
                    .addAllServices(services);

            message = builder.build();
            return message;
        }
        //EventService tasks
        if (localTask instanceof AddSelectionLimitsTask) {
            AddSelectionLimitsTask task = (AddSelectionLimitsTask)localTask;
            Message.Builder builder = EventRequest.Event.newBuilder()
                    .setLocalTask(task.getType().ordinal())
                    .setTime(localTask.getTime())
                    .addAllSelections(task.getSelections())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        //PunterService tasks
        if (localTask instanceof AddPunterLimitsTask) {
            AddPunterLimitsTask task = (AddPunterLimitsTask) localTask;
            Message.Builder builder =  PunterRequest.Punter.newBuilder()
                    .setLocalTask(localTask.getType().ordinal())
                    .setPunterId(task.getPunterId())
                    .setTime(localTask.getTime())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        if (localTask instanceof CancelWithdrawTask) {
            CancelWithdrawTask task = (CancelWithdrawTask)localTask;
            Message.Builder builder = BillingRequest.Billing.
                    newBuilder()
                    .setLocalTask(localTask.getType().ordinal())
                    .setPunterId(task.getPunterId())
                    .setTime(localTask.getTime())
                    .setSum(task.getSum())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        if (localTask instanceof CancelBetTask) {
            CancelBetTask task = (CancelBetTask)localTask;
            //BetRequest.Bet message;
            BetRequest.Bet.Builder builder = BetRequest.Bet.newBuilder()
                    .setLocalTask(task.getType().ordinal())
                    .setId(task.getBetId())
                    .setTime(localTask.getTime())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        if (localTask instanceof CancelSelectionLimitsTask) {
            CancelSelectionLimitsTask task = (CancelSelectionLimitsTask)localTask;
            Message.Builder builder = EventRequest.Event.newBuilder()
                    .setLocalTask(task.getType().ordinal())
                    .addAllSelections(task.getSelections())
                    .setTime(localTask.getTime())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        if (localTask instanceof CancelPunterLimitsTask) {
            CancelPunterLimitsTask task = (CancelPunterLimitsTask) localTask;
            Message.Builder builder =  PunterRequest.Punter.newBuilder()
                    .setLocalTask(localTask.getType().ordinal())
                    .setPunterId(task.getPunterId())
                    .setTime(localTask.getTime())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }



        else {
            throw new IllegalArgumentException("Unknown task type!");
        }
    }
}
