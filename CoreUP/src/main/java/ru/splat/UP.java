package ru.splat;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.util.Timeout;
import kamon.Kamon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.splat.actors.IdGenerator;
import ru.splat.actors.Receiver;
import ru.splat.actors.RegistryActor;
import ru.splat.db.DBConnection;
import ru.splat.db.Procedure;
import ru.splat.message.RecoverRequest;
import ru.splat.message.RecoverResponse;
import ru.splat.messages.Transaction;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.TMRecoverMsg;
import ru.splat.messages.uptm.trmetadata.MetadataPatterns;
import ru.splat.messages.uptm.trstate.TransactionState;
import ru.splat.tm.actors.TMActor;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Wraps actor system.
 */
public class UP {
    private static final String TM_ACTOR_NAME = "tm_actor";
    private static final String RECEIVER_NAME = "receiver";
    private static final String REGISTRY_NAME = "registry";
    private static final String ID_GEN_NAME = "id_gen";
    private static final int REGISTRY_SIZE = 10;
    private static final Timeout TIMEOUT = Timeout.apply(10, TimeUnit.SECONDS);
    private static final Logger LOGGER = LoggerFactory.getLogger(UP.class);

    private final ActorSystem system;
    private final ActorRef registry;
    private final ActorRef tmActor;
    private final Map<Integer, ActorRef> receivers;

    private UP(ActorSystem system, ActorRef registry, ActorRef tmActor) {
        this.system = system;
        this.registry = registry;
        this.tmActor = tmActor;
        receivers = new HashMap<>();
    }

    /**
     * Returns receiver associated with user
     * @param userId user identifier
     * @return receiver which can receive messages for user with such identifier
     */
    public ActorRef getReceiver(Integer userId) {
        return receivers.get(userId % receivers.size());
    }

    public ActorSystem getSystem() {
        return system;
    }

    //system bet
    public Proxy start() {
        ActorRef idGenerator = newActor(system, IdGenerator.class, ID_GEN_NAME);
        createReceivers(1, idGenerator, tmActor);

        Proxy proxy = Proxy.createWith(this);

        try {
            return doRecover(proxy,
                    () -> {
                        LOGGER.info("Actor system initialized.", this);
                        getSystem().scheduler()
                                .schedule(FiniteDuration.apply(1L, TimeUnit.MINUTES),
                                        FiniteDuration.apply(3L, TimeUnit.MINUTES),
                                        DBConnection::clearFinishedTransactionsAndStates,
                                        getSystem().dispatcher());
                    });
        } catch (Exception e) {
            throw new Error("Recover failed!");
        }
    }

    public static void main(String[] args) {
        UP up = UP.create();
        up.start();
    }

    //factory method for UP
    public static UP create() {
        ActorSystem system = ActorSystem.create();
        ActorRef registryActor = newActor(system, RegistryActor.class, REGISTRY_NAME, REGISTRY_SIZE);
        ActorRef tmActor = system.actorOf(Props.create(TMActor.class, registryActor)
                .withDispatcher("my-settings.akka.actor.tm-actor-dispatcher"), TM_ACTOR_NAME);
        Kamon.start();
        return new UP(system, registryActor, tmActor);
    }

    //recover procedure
    private Proxy doRecover(Proxy proxy, Procedure afterRecover) throws Exception {
        CompletableFuture<Proxy> proxyFuture = new CompletableFuture<>();

        DBConnection.processUnfinishedTransactions(trList -> {
            ExecutionContext ec = getSystem().dispatcher();
            Future<Iterable<Object>> allAnswers = Futures.sequence(sendRecoverRequests(trList), ec);

            addOnSuccessToFuture(allAnswers,
                responses -> {
                    LOGGER.info("Responses from receivers here: " + responses.toString());

                    checkResponsesPositive(responses);

                    DBConnection.getTransactionStates(states -> {
                        LOGGER.info("TransactionsStates loaded: " + states.toString());

                        Map<Long, List<ServicesEnum>> info = compareStatesAndTransactions(states, trList);
                        Future<Object> tmRecover = Patterns.ask(tmActor, new TMRecoverMsg(info), TIMEOUT);
                        addOnSuccessToFuture(tmRecover, o -> {
                            afterRecover.process();
                            proxyFuture.complete(proxy);
                        }, ec);
                    });
                }, ec);
        }, () -> {});

        return proxyFuture.get();
    }

    private static void checkResponsesPositive(Iterable<Object> responses) {
        for(Object response: responses) {
            if(!((RecoverResponse)response).isPositive()) {
                throw new Error("Recover response negative.");
            }
        }
    }

    private Iterable<Future<Object>> sendRecoverRequests(List<Transaction> trList) {
        Map<ActorRef, List<Transaction>> recoverLists;
        int size = receivers.size();

        recoverLists = trList.stream().collect(Collectors.groupingBy(
                transaction -> receivers.get(transaction.getBetInfo().getUserId() % size)
        ));

        return recoverLists.entrySet()
                .stream()
                .map(e -> Patterns.ask(e.getKey(), new RecoverRequest(e.getValue()), TIMEOUT))
                .collect(Collectors.toList());
    }

    private static <T> void addOnSuccessToFuture(Future<T> future, Consumer<T> onSuccess, ExecutionContext ec) {
        future.onSuccess(new OnSuccess<T>() {
            @Override
            public void onSuccess(T t) throws Throwable {
                onSuccess.accept(t);
            }
        }, ec);
    }

    private static Map<Long, List<ServicesEnum>> compareStatesAndTransactions(List<TransactionState> states,
                                                                              List<Transaction> trList) {
        Map<Long, List<ServicesEnum>> recoverInfo = new HashMap<>();
        List<Long> lowerBoundIdList = selectLowerBounds(trList);
        Map<Long, Pair<Transaction, TransactionState>> statesAndTransactions = connectStatesAndTransactions(states, trList);

        List<Long> firstPhaseSuccessful = selectFirstPhaseSuccessful(statesAndTransactions);

        addUnsuccessfulToRecover(recoverInfo, statesAndTransactions);
        addIdsWithListToRecover(recoverInfo, firstPhaseSuccessful, MetadataPatterns.getPhase2Services());
        addIdsWithListToRecover(recoverInfo, lowerBoundIdList, MetadataPatterns.getPhase1Services());

        return recoverInfo;
    }

    private static Map<Long, Pair<Transaction, TransactionState>> connectStatesAndTransactions(List<TransactionState> states, List<Transaction> trList) {
        Map<Long, Pair<Transaction, TransactionState>> statesAndTransactions  = new HashMap<>();
        trList.stream()
                .filter(transaction -> !transaction.getLowerBound().equals(transaction.getCurrent()))
                .forEach(transaction -> statesAndTransactions.put(transaction.getLowerBound(), new Pair<>(transaction, null)));
        states.forEach(trState -> statesAndTransactions.computeIfPresent(trState.getTransactionId(),
                (k, v) -> new Pair<>(v.first(), trState)));
        return statesAndTransactions;
    }

    private static void addIdsWithListToRecover(Map<Long, List<ServicesEnum>> recoverInfo, List<Long> idList, List<ServicesEnum> services) {
        idList.forEach(trId -> recoverInfo.put(trId, services));
    }

    private static void addUnsuccessfulToRecover(Map<Long, List<ServicesEnum>> recoverInfo, Map<Long, Pair<Transaction, TransactionState>> statesAndTransactions) {
        statesAndTransactions.values()
                .stream()
                .filter(e -> e.second() != null
                        && e.first().getState() == Transaction.State.CANCEL_RESPONDED)
                .forEach(e -> recoverInfo.put(e.first().getCurrent(), MetadataPatterns.getCancelServices(e.second())));
    }

    private static List<Long> selectFirstPhaseSuccessful(Map<Long, Pair<Transaction, TransactionState>> statesAndTransactions) {
        return statesAndTransactions.values()
                .stream()
                .filter(e -> e.second() == null
                        && e.first().getState() == Transaction.State.PHASE2_RESPONDED)
                .map(e -> e.first().getCurrent())
                .collect(Collectors.toList());
    }

    private static List<Long> selectLowerBounds(List<Transaction> trList) {
        return trList.stream()
                .filter(transaction -> transaction.getLowerBound().equals(transaction.getCurrent())
                            && transaction.getState() == Transaction.State.PHASE1_RESPONDED )
                .map(Transaction::getCurrent)
                .collect(Collectors.toList());
    }

    //create some receiver actors
    private void createReceivers(int amount, ActorRef idGenerator, ActorRef tmActor) {
        for(int i = 0; i < amount; i++) {
            createReceiver(i, idGenerator, tmActor);
        }
    }

    //create receiver associated with id_generator actor
    private void createReceiver(int index, ActorRef idGenerator, ActorRef tmActor) {
        ActorRef result = newActor(system, Receiver.class,
                RECEIVER_NAME + index,
                registry,
                idGenerator,
                tmActor);
        receivers.put(receivers.size(), result);
    }

    //creates new actor
    private static ActorRef newActor(ActorSystem system, Class<?> actorClass, String name, Object... args) {
        return system.actorOf(Props.create(actorClass, args), name);
    }

}
