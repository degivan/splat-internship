package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ru.splat.db.Bounds;
import ru.splat.message.*;
import ru.splat.messages.Transaction;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.proxyup.bet.NewRequest;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.check.CheckRequest;
import ru.splat.messages.proxyup.check.CheckResponse;
import ru.splat.messages.proxyup.check.CheckResult;
import scala.concurrent.Future;
import scala.runtime.AbstractFunction1;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static ru.splat.messages.Transaction.State;

/**
 * Actor which receives messages from users and from id_generator.
 */
public class Receiver extends LoggingActor {
    private static final int MAX_ACTIVE_PHASERS = 10_000;

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
                .match(RecoverRequest.class, this::processDoRecover)
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
        log.info("Processing CheckRequest: " + message.toString());

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
            case PHASE2_SEND:
                checkResult = CheckResult.ACCEPTED;
                break;
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
        log.info("Processing NewRequest: " + message.toString());

        BetInfo betInfo = message.getBetInfo();
        Integer userId = betInfo.getUserId();
        boolean alreadyActive = userIds.contains(userId);

        if(alreadyActive) {
            logAndDecline("Already active: " + userId, userId);    //отказ от приема новой транзакции
        } else if(current.size() > MAX_ACTIVE_PHASERS) {
            logAndDecline("Too many phasers.", userId);
        } else {
            log.info("User now active: " + userId);

            userIds.add(userId);
            current.put(userId, sender());
            idGenerator.tell(new CreateIdRequest(betInfo), self());
        }
    }

    private void logAndDecline(String logMessage, Integer userId) {
        log.info(logMessage);
        answer(new NewResponse(userId));
    }

    private void processDoRecover(RecoverRequest request) {
        log.info("Process DoRecover: " + request.toString());

        List<Transaction> transactions = request.getTransactions();
        ActorRef sender = sender();

        List<Future<Object>> futures = new ArrayList<>();

        for(Transaction transaction: transactions) {
            if(!userIds.contains(transaction.getBetInfo().getUserId())) {
                futures.add(recoverTransaction(transaction));
            } else {
                //TODO: answer back to user
                log.info("Transaction aborted: " + transaction.toString());
            }
        }
        Futures.sequence(futures, getContext().dispatcher())
                .onSuccess(new OnSuccess<Iterable<Object>>() {
                    @Override
                    public void onSuccess(Iterable<Object> objects) throws Throwable {
                        sender.tell(new RecoverResponse(true), ActorRef.noSender());
                    }
                }, getContext().dispatcher());
    }

    private Future<Object> recoverTransaction(Transaction transaction) {
        saveState(transaction);

        log.info("Creating phaser for transaction: " + transaction.toString());

        ActorRef phaser = newPhaser("phaser" + transaction.getLowerBound());

        Future<Object> future = Patterns.ask(registry,
                new RegisterRequest(new Bounds(transaction.getLowerBound(), transaction.getUpperBound()), phaser),
                Timeout.apply(10L, TimeUnit.MINUTES));
        return future.flatMap(new AbstractFunction1<Object, Future<Object>>() {
                           @Override
                           public Future<Object> apply(Object o) {
                               return Patterns.ask(phaser,
                                   new PhaserRequest(transaction),
                                   Timeout.apply(10, TimeUnit.SECONDS));
                           }
                       },
                getContext().dispatcher());
    }

    private void processTransactionReady(Transaction transaction) {
        log.info("Process TransactionReady: " + transaction.toString());

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
        log.info("Creating phaser for transaction: " + transaction.toString());

        ActorRef phaser = newPhaser("phaser" + transaction.getLowerBound());
        ActorRef receiver = self();

        Future<Object> future = Patterns.ask(registry,
                new RegisterRequest(new Bounds(transaction.getLowerBound(), transaction.getUpperBound()), phaser),
                Timeout.apply(10L, TimeUnit.MINUTES));

        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Throwable {
                phaser.tell(new PhaserRequest(transaction), receiver);
            }
        }, getContext().dispatcher());
    }

    private void processRequestResult(Transaction transaction) {
        log.info("Process RequestResult: " + transaction.toString());

        freeUser(transaction.getBetInfo().getUserId());
        saveState(transaction);
    }

    private void freeUser(Integer userId) {
        userIds.remove(userId);
        current.remove(userId);

        log.info("User no more active: " + userId);
    }

    private void saveState(Transaction transaction) {
        results.put(transaction.getLowerBound(), transaction.getState());
    }

    private void answer(Object msg) {
        sender().tell(msg, self());
    }

    private ActorRef newPhaser(String name) {
        return context().actorOf(Props.create(PhaserActor.class, tmActor, self(), registry)
                .withDispatcher("my-settings.akka.actor.phaser-dispatcher"), name);
    }


}
