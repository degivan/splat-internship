package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import ru.splat.LoggerGlobal;
import ru.splat.db.DBConnection;
import ru.splat.message.PhaserRequest;
import ru.splat.messages.Transaction;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trmetadata.MetadataPatterns;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.messages.uptm.trstate.TransactionState;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Иван on 15.12.2016.
 */
public class PhaserActor extends UntypedActor {
    private final ActorRef tmActor;
    private final ActorRef receiver;

    private Transaction transaction;
    private Procedure<Object> phase2 = new Phase2();
    private Procedure<Object> cancel = new Cancel();

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

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof PhaserRequest) {
            processPhaserRequest((PhaserRequest) message);
        } else if(message instanceof TransactionState) {
            processTransactionState((TransactionState) message);
        } else if(message instanceof ReceiveTimeout) {
            processReceiveTimeout();
        } else if(message instanceof TMResponse) {
            //TODO: Change state to PHASE1_RESPONDED
        } else {
            unhandled(message);
        }
    }

    private void processReceiveTimeout() {
        LoggerGlobal.log("Timeout received in phaser for transaction: " + transaction.toString());

        saveDBWithStateCancel(Transaction.State.CANCEL);
    }

    private void processTransactionState(TransactionState o) {
        LoggerGlobal.log("Processing TransactionState: " + o.toString());

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
        LoggerGlobal.log("Result send to receiver for transaction: " + transaction.toString());

        receiver.tell(transaction, getSelf());
    }

    private void processPhaserRequest(PhaserRequest o) {
        LoggerGlobal.log("Process PhaserRequest: " + o.toString());

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

    private void sendPhase2(Transaction transaction) {
        LoggerGlobal.log("Sending phase2 for transaction: " +transaction.toString());

        sendMetadataAndAfter(MetadataPatterns::createPhase2,
                transaction,
                v -> getContext().become(phase2));
    }

    private void cancelTransaction(Transaction transaction) {
        sendMetadataAndAfter(MetadataPatterns::createCancel,
                transaction,
                v -> getContext().become(cancel));
    }

    private void processNewTransaction(Transaction transaction) {
        sendMetadataAndAfter(MetadataPatterns::createPhase1,
                transaction,
                v -> getContext().setReceiveTimeout(Duration.apply(10L, TimeUnit.SECONDS)));
    }

    private void sendMetadataAndAfter(Function<Transaction, TransactionMetadata> metadataBuilder,
                                      Transaction transaction, Consumer<Void> after) {
        TransactionMetadata trMetadata = metadataBuilder.apply(transaction);
        tmActor.tell(trMetadata, getSelf());

        after.accept(null);
    }

    private static boolean isResponsePositive(TransactionState transactionState) {
        return transactionState
                .getLocalStates()
                .values()
                .stream()
                .allMatch(ServiceResponse::isPositive);
    }

    private abstract class State implements Procedure<Object> {
        @Override
        public void apply(Object message) throws Exception {
            if(message instanceof TransactionState) {
                processTransactionState((TransactionState) message);
            } else if(message instanceof ReceiveTimeout) {
                //do nothing
            } else if(message instanceof TMResponse) {
                //TODO: check that it's confirmation for phase2 and change state
            }
        }

        //TODO
        boolean checkIdCorrect(TransactionState trState, Transaction transaction) {
            return (trState.getTransactionId()).equals(transaction.getCurrent());
        }

        void processTransactionState(TransactionState trState) {
            LoggerGlobal.log("Processing " + trState.toString() + " in context: " + getContext().toString());
        }
    }

    private class Phase2 extends State {
        @Override
        void processTransactionState(TransactionState trState) {
            super.processTransactionState(trState);

            if(checkIdCorrect(trState, transaction)) {
                if(isResponsePositive(trState)) {
                    transaction.setState(Transaction.State.COMPLETED);
                    getContext().stop(getSelf());
                } else {
                    //can stage2 not pass???
                }
            }
        }

    }

    private class Cancel extends State {
        @Override
        void processTransactionState(TransactionState trState) {
            super.processTransactionState(trState);

            if(checkIdCorrect(trState, transaction)) {
                if(isResponsePositive(trState)) {
                    transaction.setState(Transaction.State.CANCEL_COMPLETED);
                    getContext().stop(getSelf());
                } else {
                    //can stage2 not pass???
                }
            }
        }
    }
}
