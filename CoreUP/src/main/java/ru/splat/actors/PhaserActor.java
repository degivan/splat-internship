package ru.splat.actors;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import ru.splat.db.DBConnection;
import ru.splat.message.PhaserRequest;
import ru.splat.messages.Transaction;
import ru.splat.messages.uptm.TMConfirm;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trmetadata.MetadataPatterns;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
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

    @Override
    public void onReceive(Object o) throws Throwable {
        if(o instanceof PhaserRequest) {
            processPhaserRequest((PhaserRequest) o);
        } else if(o instanceof TMResponse) {
            processTMResponse((TMResponse) o);
        } else if(o instanceof ReceiveTimeout) {
            processReceiveTimeout();
        } else if(o instanceof TMConfirm) {
            //TODO: Change state to PHASE1_RESPONDED
        }
    }

    private void processReceiveTimeout() {
        transaction.setState(Transaction.State.CANCEL);

        DBConnection.resaveTransaction(transaction,
                () -> {
                    cancelTransaction(transaction);
                    sendResult(transaction);
                });
    }

    private void processTMResponse(TMResponse o) {
        if(isResponsePositive(o)) {
            transaction.setState(Transaction.State.PHASE2_SEND);

            DBConnection.resaveTransaction(transaction,
                    () ->  {
                        sendPhase2(transaction);
                        sendResult(transaction);
                    });
        } else {
            transaction.setState(Transaction.State.DENIED);

            DBConnection.resaveTransaction(transaction,
                    () -> sendResult(transaction));
        }
    }

    private void sendResult(Transaction transaction) {
        receiver.tell(transaction, getSelf());
    }

    private void processPhaserRequest(PhaserRequest o) {
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
        //TODO change active transaction_id
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

    //TODO:
    private static boolean isResponsePositive(TMResponse o) {
        return false;
    }

    private abstract class State implements Procedure<Object> {
        @Override
        public void apply(Object o) throws Exception {
            if(o instanceof TMResponse) {
                processTMResponse((TMResponse) o);
            } else if(o instanceof ReceiveTimeout) {
                //do nothing
            } else if(o instanceof TMConfirm) {
                //TODO: check that it's confirmation for phase2 and change state
            }
        }

        //TODO
        boolean checkIdCorrect(TMResponse o, Transaction transaction) {
            return (o.getTransactionId()).equals(transaction.getTransactionId());
        }

        abstract void processTMResponse(TMResponse o);
    }

    private class Phase2 extends State {
        @Override
        void processTMResponse(TMResponse o) {
            if(checkIdCorrect(o, transaction)) {
                if(isResponsePositive(o)) {
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
        void processTMResponse(TMResponse o) {
            if(checkIdCorrect(o, transaction)) {
                if(isResponsePositive(o)) {
                    transaction.setState(Transaction.State.CANCEL_COMPLETED);
                    getContext().stop(getSelf());
                } else {
                    //can stage2 not pass???
                }
            }
        }
    }
}
