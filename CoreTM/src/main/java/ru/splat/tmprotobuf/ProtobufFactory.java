package ru.splat.tmprotobuf;

import com.google.protobuf.Message;
import ru.splat.messages.BetRequest;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.*;
import ru.splat.messages.uptm.trmetadata.billing.*;
import ru.splat.messages.uptm.trmetadata.bet.*;
import ru.splat.messages.uptm.trmetadata.event.*;
import ru.splat.messages.uptm.trmetadata.punter.*;


import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Дмитрий on 17.01.2017.
 */
//простая фабрика протобуфов
    //TO-DO: необходимо протестировать возможность добавления пустых полей
public  class ProtobufFactory {
    public ProtobufFactory() {
    }
    //подготовить мессаги для оптравки
    public static Message buildProtobuf(LocalTask localTask, Set<ServicesEnum> _services) throws Exception{
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
                    .setSum(task.getSum())
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
                    .addAllSelections(task.getSelections())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }

        if (localTask instanceof CancelSelectionLimitsTask) {
            CancelSelectionLimitsTask task = (CancelSelectionLimitsTask)localTask;
            Message.Builder builder = EventRequest.Event.newBuilder()
                    .setLocalTask(task.getType().ordinal())
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
                    .addAllServices(services);
            message = builder.build();
            return message;
        }

        if (localTask instanceof CancelPunterLimitsTask) {
            CancelPunterLimitsTask task = (CancelPunterLimitsTask) localTask;
            Message.Builder builder =  PunterRequest.Punter.newBuilder()
                    .setLocalTask(localTask.getType().ordinal())
                    .setPunterId(task.getPunterId())
                    .addAllServices(services);
            message = builder.build();
            return message;
        }
        else {
            throw new Exception("Unknown task type!");
        }
    }
}
