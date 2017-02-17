package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.japi.pf.UnitPFBuilder;
import ru.splat.db.DBConnection;
import ru.splat.message.PhaserRequest;
import ru.splat.message.PhaserResponse;
import ru.splat.messages.Transaction;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trmetadata.MetadataPatterns;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.messages.uptm.trstate.TransactionState;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Иван on 15.12.2016.
 */
public class PhaserActor extends LoggingActor {
    private final ActorRef tmActor;
    private final ActorRef receiver;

    private Transaction transaction;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PhaserRequest.class, this::processPhaserRequest)
                .match(TransactionState.class, this::processTransactionState)
                .match(ReceiveTimeout.class, m -> processReceiveTimeout())
                .match(TMResponse.class, m -> {/*TODO: Change state to PHASE1_RESPONDED */})
                .matchAny(this::unhandled).build();
    }

    public PhaserActor(ActorRef tmActor, ActorRef receiver) {
        this.tmActor = tmActor;
        this.receiver = receiver;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    //TODO: stop correctly
    @Override
    public void postStop() throws Exception {
        super.postStop();

        log.info("Phaser stopped.");
    }



    private void processPhaserRequest(PhaserRequest o) {
        log.info("Process PhaserRequest: " + o.toString());

        transaction = o.getTransaction();
        context().setReceiveTimeout(Duration.apply(10L, TimeUnit.SECONDS));
        
        switch(transaction.getState()) {
            case CREATED:
                processNewTransaction(transaction);
                break;
            case PHASE2_SEND:
                sendPhase2(transaction);
                break;
            default: //CANCEL OR DENIED
                DBConnection.findTransactionState(transaction.getLowerBound(),
                        tState -> cancelTransaction(transaction, tState));
        }
    }

    private void processReceiveTimeout() {
        log.info("Timeout received in phaser for transaction: " + transaction.toString());

        becomeAndLog(timeout());
    }

    private void processTransactionState(TransactionState trState) {
        log.info("Processing TransactionState: " + trState.toString());

        updateBetId(trState, transaction);

        if(isResponsePositive(trState)) {
            saveDBWithState(Transaction.State.PHASE2_SEND,
                    () -> {
                        sendPhase2(transaction);
                        sendResult(transaction);
                    });
        } else {
            saveDBWithStateCancel(Transaction.State.DENIED, trState);
        }
    }

    private static void updateBetId(TransactionState o, Transaction transaction) {
        Long betId = (Long) o.getLocalStates().get(ServicesEnum.BetService).getAttachment();
        transaction.getBetInfo().setBetId(betId);
    }

    private void saveDBWithState(Transaction.State state, ru.splat.db.Procedure after) {
        transaction.nextState(state);
        DBConnection.overwriteTransaction(transaction, after);
    }

    private void saveDBWithStateCancel(Transaction.State state, TransactionState trState) {
        DBConnection.addTransactionState(trState,
                tState -> saveDBWithState(state,
                        () -> {
                            cancelTransaction(transaction, trState);
                            sendResult(transaction);
                        }));
    }

    private void sendResult(Transaction transaction) {
        log.info("Result send to receiver for transaction: " + transaction.toString());

        receiver.tell(new PhaserResponse(transaction), self());
    }

    private void sendPhase2(Transaction transaction) {
        log.info("Sending phase2 for transaction: " + transaction.toString());

        sendMetadataAndAfter(MetadataPatterns.createPhase2(transaction),
                v -> becomeAndLog(phase2()));
    }

    private void cancelTransaction(Transaction transaction, TransactionState trState) {
        log.info("Sending cancel for transaction: " + transaction.toString());

        sendMetadataAndAfter(MetadataPatterns.createCancel(transaction, trState),
                v -> becomeAndLog(cancel()));
    }

    private void becomeAndLog(PartialFunction<Object, BoxedUnit> state) {
        log.info("Phaser: " + this.toString() + " changes to state: " + state);

        context().become(state);
    }

    private void processNewTransaction(Transaction transaction) {
        sendMetadataAndAfter(MetadataPatterns.createPhase1(transaction), v -> {});
    }

    private void sendMetadataAndAfter(TransactionMetadata trMetadata, Consumer<Void> after) {
        tmActor.tell(trMetadata, self());

        after.accept(null);
    }

    private static boolean isResponsePositive(TransactionState transactionState) {
        return transactionState
                .getLocalStates()
                .values()
                .stream()
                .allMatch(ServiceResponse::isPositive);
    }

    private PartialFunction<Object, BoxedUnit> timeout() {
        return state().match(TransactionState.class,
                trState -> {
                    logTransactionState(trState);
                    updateBetId(trState, transaction);
                    saveDBWithStateCancel(Transaction.State.CANCEL, trState);
                }).build();
    }

    private PartialFunction<Object, BoxedUnit> phase2(){
        return transactionStateReceiver(Transaction.State.COMPLETED);
    }

    private PartialFunction<Object, BoxedUnit> cancel(){
        return transactionStateReceiver(Transaction.State.CANCEL_COMPLETED);
    }

    private PartialFunction<Object, BoxedUnit> transactionStateReceiver(Transaction.State dbState) {
        return state().match(TransactionState.class,
                trState -> {
                    logTransactionState(trState);
                    if(checkIdCorrect(trState, transaction)) {
                        if(isResponsePositive(trState)) {
                            saveDBWithState(dbState,
                                    () -> context().stop(self()));
                        } else {
                            //can stage2 not pass???
                        }
                    }
                }).build();
    }

    private static boolean checkIdCorrect(TransactionState trState, Transaction transaction) {
        return (trState.getTransactionId()).equals(transaction.getCurrent());
    }

    private void logTransactionState(TransactionState trState) {
        log.info("Processing " + trState.toString() + " in context: " + getContext().toString());
    }

    private static UnitPFBuilder<Object> state() {
        UnitPFBuilder<Object> builder = new UnitPFBuilder<>();

        builder.match(TMResponse.class,
                m -> {/* TODO: check that it's confirmation for phase2 and change state */});

        return builder;
    }
}