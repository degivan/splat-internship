package ru.splat.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.japi.pf.UnitPFBuilder;
import ru.splat.LoggerGlobal;
import ru.splat.db.DBConnection;
import ru.splat.message.PhaserRequest;
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
import java.util.function.Function;

/**
 * Created by Иван on 15.12.2016.
 */
public class PhaserActor extends AbstractActor {
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
    }



    private void processPhaserRequest(PhaserRequest o) {
        LoggerGlobal.log("Process PhaserRequest: " + o.toString(), this);

        transaction = o.getTransaction();

        switch(transaction.getState()) {
            case CREATED:
                processNewTransaction(transaction);
                break;
            case CANCEL:
                cancelTransaction(transaction);
                break;
            case PHASE2_SEND:
                sendPhase2(transaction);
                break;
        }
    }

    private void processReceiveTimeout() {
        LoggerGlobal.log("Timeout received in phaser for transaction: " + transaction.toString(), this);

        becomeAndLog(timeout());
    }

    private void processTransactionState(TransactionState o) {
        LoggerGlobal.log("Processing TransactionState: " + o.toString(), this);

        updateBetId(o, transaction);

        if(isResponsePositive(o)) {
            saveDBWithState(Transaction.State.PHASE2_SEND,
                    () -> {
                        sendPhase2(transaction);
                        sendResult(transaction);
                    });
        } else {
            saveDBWithStateCancel(Transaction.State.DENIED);
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

    private void saveDBWithStateCancel(Transaction.State state) {
        saveDBWithState(state,
                () -> {
                    cancelTransaction(transaction);
                    sendResult(transaction);
                });
    }

    private void sendResult(Transaction transaction) {
        LoggerGlobal.log("Result send to receiver for transaction: " + transaction.toString(), this);

        receiver.tell(transaction, self());
    }

    private void sendPhase2(Transaction transaction) {
        LoggerGlobal.log("Sending phase2 for transaction: " + transaction.toString(), this);

        sendMetadataAndAfter(MetadataPatterns::createPhase2,
                transaction,
                v -> becomeAndLog(phase2()));
    }

    private void cancelTransaction(Transaction transaction) {
        LoggerGlobal.log("Sending cancel for transaction: " + transaction.toString(), this);

        sendMetadataAndAfter(MetadataPatterns::createCancel,
                transaction,
                v -> becomeAndLog(cancel()));
    }

    private void becomeAndLog(PartialFunction<Object, BoxedUnit> state) {
        LoggerGlobal.log("Phaser: " + this.toString() + " changes to state: " + state, this);

        context().become(state);
    }

    private void processNewTransaction(Transaction transaction) {
        sendMetadataAndAfter(MetadataPatterns::createPhase1,
                transaction,
                v -> context().setReceiveTimeout(Duration.apply(10L, TimeUnit.SECONDS)));
    }

    private void sendMetadataAndAfter(Function<Transaction, TransactionMetadata> metadataBuilder,
                                      Transaction transaction, Consumer<Void> after) {
        TransactionMetadata trMetadata = metadataBuilder.apply(transaction);
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
                    saveDBWithStateCancel(Transaction.State.CANCEL);
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
        LoggerGlobal.log("Processing " + trState.toString() + " in context: " + getContext().toString(), this);
    }

    private static UnitPFBuilder<Object> state() {
        UnitPFBuilder<Object> builder = new UnitPFBuilder<>();

        builder.match(TMResponse.class,
                m -> {/* TODO: check that it's confirmation for phase2 and change state */});

        return builder;
    }


}
