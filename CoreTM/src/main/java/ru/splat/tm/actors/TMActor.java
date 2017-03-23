package ru.splat.tm.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.splat.kafka.serializer.ProtoBufMessageSerializer;
import ru.splat.messages.conventions.ServicesEnum;
import ru.splat.messages.uptm.TMRecoverMsg;
import ru.splat.messages.uptm.TMRecoverResponse;
import ru.splat.messages.uptm.TMResponse;
import ru.splat.messages.uptm.trmetadata.LocalTask;
import ru.splat.messages.uptm.trmetadata.TransactionMetadata;
import ru.splat.messages.uptm.trstate.ServiceResponse;
import ru.splat.messages.uptm.trstate.TransactionState;
import ru.splat.messages.uptm.trstate.TransactionStateMsg;
import ru.splat.tm.messages.*;
import ru.splat.tm.protobuf.ProtobufFactory;
import ru.splat.tm.util.RequestTopicMapper;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Created by Дмитрий on 05.01.2017.
 */
public  class TMActor extends AbstractActor {
    private KafkaProducer<Long, Message> producer;
    private Map<Long, TransactionState> states = new HashMap<>();
    private final ActorRef registry;
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final ActorRef consumerActor;
    private static final String TM_CONSUMER_NAME = "tm_consumer";
    private static final long RETRY_SEND_INTERVAL = 2000;
    private int initCount = 0;
    private int commitCount = 0;
    private static final long LOG_COUNTERS_INTERVAL = 30; //в секундах

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TransactionMetadata.class, this::processTransaction)
                .match(TaskSentMsg.class, this::processSent)
                .match(RetrySendMsg.class, m ->
                {
                    log.info("processing RetrySendMsg for " + m.getTransactionId() + " to topic " + m.getTopic());
                    send(m.getTopic(), m.getTransactionId(), m.getMessage());})
                .match(ServiceResponseMsg.class, this::processResponse)
                .match(TMRecoverMsg.class, this::processRecover)
                .match(TMCommitTransactionMsg.class, this::commitTransaction)
                .match(LogCountersMsg.class, this::logCounters)
                .matchAny(this::unhandled)
                .build();
    }
    //Вывод числа отправленных и завершенных транзакций
    private void logCounters(LogCountersMsg m) {
        log.info("Processed transaction phases: " + (initCount / LOG_COUNTERS_INTERVAL) + " ph/sec");
        log.info("Finished transaction phases: " + (commitCount/ LOG_COUNTERS_INTERVAL) + " ph/sec");
        initCount = 0;
        commitCount = 0;
        getContext().system().scheduler().scheduleOnce(Duration.create(LOG_COUNTERS_INTERVAL, TimeUnit.SECONDS),
                getSelf(), new LogCountersMsg(), getContext().dispatcher(), null);
    }

    //создание стейта транзакции из метадаты
    private void createTransactionState(TransactionMetadata transactionMetadata) {
        long trId = transactionMetadata.getTransactionId();
        Map<ServicesEnum, ServiceResponse> responseMap = new HashMap<>();
        transactionMetadata.getLocalTasks().forEach(localTask -> {
            responseMap.put(localTask.getService(), new ServiceResponse());    //создание "пустых ответов от сервисов"
        });
        TransactionState transactionState = new TransactionState(trId,responseMap);
        states.put(trId, transactionState);
    }

    private void processRecover(TMRecoverMsg m) {
        log.info("processing TMRecoverMsg with " + m.getTransactions().size() + " transactions");
        m.getTransactions().forEach((id, servicesList) -> {
            Map<ServicesEnum, ServiceResponse> responseMap = servicesList.stream()
                    .collect(Collectors.toMap((servicesEnum) -> servicesEnum,  (servicesEnum) ->  (new ServiceResponse())));
            states.put(id, new TransactionState(id, responseMap));
        });
        Timeout timeout = new Timeout(Duration.create(20, "seconds"));
        Future<Object> recoverFuture = Patterns.ask(consumerActor,
                new TMConsumerRecoverMsg(), timeout);
        try {
            TMConsumerRecoverResponse consumerRecoverResponse = (TMConsumerRecoverResponse) Await.result(recoverFuture,  timeout.duration());
            if (consumerRecoverResponse.isSuccessful()) {
                log.info("successful recover");
                sender().tell(TMRecoverResponse.confirmTMRecover(), getSelf()); //а сендера-то нету
                consumerActor.tell(new PollMsg(), getSelf());
                getSelf().tell(new LogCountersMsg(), getSelf());
            }
            else {
                log.info("recover failure");
                sender().tell(TMRecoverResponse.rejectTMRecover("Consumer failed to connect to kafka"), getSelf());
                consumerActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
                producer.close();
                getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("recover failure");
            sender().tell(TMRecoverResponse.rejectTMRecover("Consumer failed to connect to kafka: " + e.getMessage()), getSelf());
            consumerActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
        }


    }



    private void processTransaction(TransactionMetadata trMetadata) {
        initCount++;
        createTransactionState(trMetadata);
        List<LocalTask> taskList = trMetadata.getLocalTasks();
        long transactionId = trMetadata.getTransactionId();
        log.info("processing transaction " + transactionId + " with " + taskList.size() + " tasks");
        Set<ServicesEnum> services = taskList.stream().map(LocalTask::getService)
                .collect(Collectors.toSet());
        taskList.forEach(task->{
            Message message = ProtobufFactory.buildProtobuf(task, services);
            send(RequestTopicMapper.getTopic(task.getService()), transactionId, message);
        });
    }
    private void send(String topic, Long transactionId, Message message) {
        producer.send(new ProducerRecord<>(topic, transactionId, message),
                (metadata, e) -> {
                    if (e != null)
                        if (e instanceof SocketTimeoutException) {
                            log.info("kafka connection issues");
                            getContext().system().scheduler().scheduleOnce(Duration.create(RETRY_SEND_INTERVAL, TimeUnit.MILLISECONDS),
                                    getSelf(), new RetrySendMsg(topic, transactionId, message), getContext().dispatcher(), null);
                        }
                        else
                            getSelf().tell(new RetrySendMsg(topic, transactionId, message), getSelf());
                    else getSelf().tell(new TaskSentMsg(transactionId, RequestTopicMapper.getService(topic)), getSelf());
                });
    }
    private void processResponse(ServiceResponseMsg serviceResponseMsg) {
        long trId = serviceResponseMsg.getTransactionId();
        if (!states.containsKey(trId)) {    //эта проверка нужна, так как возможна трудноустранимая гонка с асинхронным ответом от producer.send
            consumerActor.tell(new MarkSpareMsg(trId, serviceResponseMsg.getService(), serviceResponseMsg.getOffset()), getSelf());
            return;
        }
        ServiceResponse response = serviceResponseMsg.getMessage();
        states.get(trId).getLocalStates()
                .put(serviceResponseMsg.getService(), response);
        TransactionState transactionState = states.get(trId);
        if (transactionState.getLocalStates()
                .entrySet().stream().map(state -> state.getValue().isResponseReceived())
                .allMatch(e -> e)) {
            log.info("all responses for transaction " + trId + " are received");
            registry.tell(new TransactionStateMsg(transactionState, () -> getSelf().tell(new TMCommitTransactionMsg(trId), getSelf())), getSelf());
        }
    }
    //сообщить консюмеру, что можно коммитить транзакцию trId в топиках
    private void commitTransaction(TMCommitTransactionMsg m) {
        commitCount++;
        log.info("commitTransaction " + m.getTransactionId());
        if (!states.containsKey(m.getTransactionId())) {
            return;
        }
        consumerActor.tell(
                new CommitTransactionMsg(m.getTransactionId(), states.get(m.getTransactionId()).getLocalStates().keySet().stream().collect(Collectors.toSet())),
                getSelf());
        states.remove(m.getTransactionId());
    }
    private void processSent(TaskSentMsg m) {
        long trId = m.getTransactionId();
        if (!states.containsKey(trId)) {
            log.info("caught stale producer response: " + trId);
            return;
        }
        states.get(trId).getLocalStates()
                .get(m.getService()).setRequestSent(true);
        if (states.get(trId).getLocalStates()
                .entrySet().stream().map(state -> state.getValue().isRequestSent())
                .allMatch(e -> e)) {
            log.info("all requests for transaction " + trId + " are sent to services");
            registry.tell(new TMResponse(trId), getSelf());
        }
    }

    public TMActor(ActorRef registry) {
        Properties propsProducer = new Properties();
        propsProducer.put("bootstrap.servers", "localhost:9092");
        propsProducer.put("acks", "all");
        propsProducer.put("retries", 0);
        propsProducer.put("batch.size", 16384);
        propsProducer.put("linger.ms", 1);
        propsProducer.put("buffer.memory", 33554432);
        producer = new KafkaProducer(propsProducer, new LongSerializer(), new ProtoBufMessageSerializer());
        this.registry = registry;
        log.info("TMActor is initialized");
        consumerActor = getContext().actorOf(Props.create(TMConsumerActor.class).
                withDispatcher("my-settings.akka.actor.tm-consumer-dispatcher"), TM_CONSUMER_NAME);

    }
}


