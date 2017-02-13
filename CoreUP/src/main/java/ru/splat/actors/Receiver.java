package ru.splat.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ru.splat.LoggerGlobal;
import ru.splat.message.*;
import ru.splat.messages.Transaction;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.proxyup.bet.NewRequest;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.check.CheckRequest;
import ru.splat.messages.proxyup.check.CheckResponse;
import ru.splat.messages.proxyup.check.CheckResult;
import scala.concurrent.Future;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ru.splat.messages.Transaction.State;

/**
 * Actor which receives messages from users and from id_generator.
 */
public class Receiver extends AbstractActor {
    private static final String NOT_ACTIVE_TR = "TRANSACTION IS NOT IN PROCESS";

    private final ActorRef registry;
    private final ActorRef idGenerator;
    private final ActorRef tmActor;

    private final Set<Integer> userIds;
    private final Map<Long, Transaction.State> results;
    private final Map<Integer, ActorRef> current;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(NewRequest.class, this::processNewRequest)
                .match(CheckRequest.class, this::processCheckRequest)
                .match(CreateIdResponse.class,
                        m -> processTransactionReady(m.getTransaction()))
                .match(RecoverRequest.class,
                        m -> processDoRecover(m.getTransaction()))
                .match(PhaserResponse.class,
                        m -> processRequestResult(m.getTransaction()))
                .matchAny(this::unhandled).build();
    }

    public Receiver(ActorRef registry, ActorRef idGenerator, ActorRef tmActor) {
        this.registry = registry;
        this.idGenerator = idGenerator;
        this.tmActor = tmActor;

        userIds = new HashSet<>();
        results = new HashMap<>();
        current = new HashMap<>();
    }

    private void processCheckRequest(CheckRequest message) {
        LoggerGlobal.log("Processing CheckRequest: " + message.toString());

        State state = results.get(message.getTransactionId());
        if(state == null) {
            answer(new CheckResponse(message.getUserId(), CheckResult.NOT_ACTIVE_TR));
        } else {
            answer(stateToCheckResponse(message.getUserId(), state));
        }
    }

    private static CheckResponse stateToCheckResponse(Integer userId, State state) {
        CheckResult checkResult;
        switch(state) {
            case CREATED:
                checkResult = CheckResult.PENDING;
                break;
            case CANCEL:
                checkResult = CheckResult.CANCELLED;
                break;
            case DENIED:
                checkResult = CheckResult.REJECTED;
                break;
            default:
                checkResult = CheckResult.ACCEPTED;
        }
        return new CheckResponse(userId, checkResult);
    }

    private void processNewRequest(NewRequest message) {
        LoggerGlobal.log("Processing NewRequest: " + message.toString());

        BetInfo betInfo = message.getBetInfo();
        Integer userId = betInfo.getUserId();
        boolean alreadyActive = userIds.contains(userId);

        if(alreadyActive) {
            LoggerGlobal.log("Already active: " + userId);
            answer("ALREADY ACTIVE");
        } else {
            LoggerGlobal.log("User now active: " + userId);

            userIds.add(userId);
            current.put(userId, sender());
            idGenerator.tell(new CreateIdRequest(betInfo), self());
        }
    }

    private void processDoRecover(Transaction transaction) {
        LoggerGlobal.log("Process DoRecover: " + transaction.toString());

        if(!userIds.contains(transaction.getBetInfo().getUserId())) {
            startTransaction(transaction);
        } else {
            //TODO: answer back to user
            LoggerGlobal.log("Transaction aborted: " + transaction.toString());
        }
    }

    private void processTransactionReady(Transaction transaction) {
        LoggerGlobal.log("Process TransactionReady: " + transaction.toString());

        Integer userId = transaction.getBetInfo().getUserId();
        Long trId = transaction.getLowerBound();

        startTransaction(transaction);
        current.get(userId)
                .tell(new NewResponse(trId, userId), self());
    }

    private void startTransaction(Transaction transaction) {
        saveState(transaction);
        createPhaser(transaction);
    }

    private void createPhaser(Transaction transaction) {
        LoggerGlobal.log("Creating phaser for transaction: " + transaction.toString());

        ActorRef phaser = newActor(PhaserActor.class, "phaser" + transaction.getLowerBound(), tmActor, self());
        ActorRef receiver = self();

        Future<Object> future = Patterns.ask(registry,
                new RegisterRequest(transaction.getLowerBound(), phaser),
                Timeout.apply(10L, TimeUnit.MINUTES));

        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Throwable {
                phaser.tell(new PhaserRequest(transaction), receiver);
            }
        }, getContext().dispatcher());
    }

    private void processRequestResult(Transaction transaction) {
        LoggerGlobal.log("Process RequestResult: " + transaction.toString());

        saveState(transaction);
    }

    private void saveState(Transaction transaction) {
        results.put(transaction.getLowerBound(), transaction.getState());
    }

    private void answer(Object msg) {
        sender().tell(msg, self());
    }

    private ActorRef newActor(Class<?> clazz, String name, Object... args) {
        return getContext().actorOf(Props.create(clazz, args), name);
    }


}
