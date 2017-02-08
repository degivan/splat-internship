package ru.splat.messages.uptm.trmetadata;

import ru.splat.messages.Transaction;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * TODO:
 */
public class MetadataPatterns {
    //phase1 commands
    public static TransactionMetadata createPhase1(Transaction transaction) {
        return createMetadataWithTasks(transaction,
                BillingWithdrawTask::create,
                AddSelectionLimitsTask::create,
                AddPunterLimitsTask::create,
                AddBetTask::create);
    }

    //cancel commands
    public static TransactionMetadata createCancel(Transaction transaction) {
        return createMetadataWithTasks(transaction,
                CancelWithdrawTask::create,
                CancelSelectionLimitsTask::create,
                CancelPunterLimitsTask::create,
                CancelBetTask::create);
    }

    //phase2 commands
    public static TransactionMetadata createPhase2(Transaction transaction) {
        return createMetadataWithTasks(transaction,
                FixBetTask::create);
    }

    @SafeVarargs
    private static TransactionMetadata createMetadataWithTasks(Transaction transaction,
                                                               Function<BetInfo, LocalTask>... builders) {
        return new TransactionMetadata(transaction.getCurrent(),
                tasksFrom(Arrays.asList(builders), transaction.getBetInfo()));
    }

    private static List<LocalTask> tasksFrom(List<Function<BetInfo, LocalTask>> builders,
                                             BetInfo betInfo) {
        List<LocalTask> tasks = new ArrayList<>();
        builders.forEach(b -> tasks.add(b.apply(betInfo)));
        return tasks;
    }

}
