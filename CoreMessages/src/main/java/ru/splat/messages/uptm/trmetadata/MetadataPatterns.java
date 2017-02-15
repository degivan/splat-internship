package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.Transaction;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.uptm.trmetadata.bet.AddBetTask;
import ru.splat.messages.uptm.trmetadata.bet.CancelBetTask;
import ru.splat.messages.uptm.trmetadata.bet.FixBetTask;
import ru.splat.messages.uptm.trmetadata.billing.BillingWithdrawTask;
import ru.splat.messages.uptm.trmetadata.billing.CancelWithdrawTask;
import ru.splat.messages.uptm.trmetadata.event.AddSelectionLimitsTask;
import ru.splat.messages.uptm.trmetadata.event.CancelSelectionLimitsTask;
import ru.splat.messages.uptm.trmetadata.punter.AddPunterLimitsTask;
import ru.splat.messages.uptm.trmetadata.punter.CancelPunterLimitsTask;
import ru.splat.messages.uptm.trstate.TransactionState;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static ru.splat.messages.conventions.ServicesEnum.*;

/**
 * TODO:
 */
public class MetadataPatterns {
    private static final Map<ServicesEnum, Function<BetInfo, LocalTask>> cancelServices = new HashMap<>();

    static {
        cancelServices.put(BillingService, CancelWithdrawTask::create);
        cancelServices.put(EventService, CancelSelectionLimitsTask::create);
        cancelServices.put(PunterService, CancelPunterLimitsTask::create);
    }

    //phase1 commands
    public static TransactionMetadata createPhase1(Transaction transaction) {
        return createMetadataWithTasks(transaction,
                asList(
                    BillingWithdrawTask::create,
                    AddSelectionLimitsTask::create,
                    AddPunterLimitsTask::create,
                    AddBetTask::create));
    }

    //cancel commands
    public static TransactionMetadata createCancel(Transaction transaction, TransactionState trState) {
        List<Function<BetInfo, LocalTask>> tasks = cancelServices.entrySet()
                .stream()
                .filter(e -> trState.getLocalStates().get(e.getKey()).isPositive())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        tasks.add(CancelBetTask::create);

        return createMetadataWithTasks(transaction, tasks);
    }

    //phase2 commands
    public static TransactionMetadata createPhase2(Transaction transaction) {
        return createMetadataWithTasks(transaction,
                singletonList(FixBetTask::create));
    }

    private static TransactionMetadata createMetadataWithTasks(Transaction transaction,
                                                               List<Function<BetInfo, LocalTask>> builders) {
        return new TransactionMetadata(transaction.getCurrent(),
                tasksFrom(builders, transaction.getBetInfo()));
    }

    private static List<LocalTask> tasksFrom(List<Function<BetInfo, LocalTask>> builders,
                                             BetInfo betInfo) {
        List<LocalTask> tasks = new ArrayList<>();
        builders.forEach(b -> tasks.add(b.apply(betInfo)));
        return tasks;
    }

}
