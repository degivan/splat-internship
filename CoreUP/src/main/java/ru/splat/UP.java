package ru.splat;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ru.splat.actors.IdGenerator;
import ru.splat.actors.Receiver;
import ru.splat.actors.RegistryActor;
import ru.splat.db.DBConnection;
import ru.splat.db.Procedure;
import ru.splat.message.RecoverRequest;
import ru.splat.tm.actors.TMActor;
import ru.splat.tm.actors.TMConsumerActor;
import ru.splat.tm.messages.PollMsg;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Wraps actor system.
 */
public class UP {
    private static final String TM_ACTOR_NAME = "tm_actor";
    private static final String RECEIVER_NAME = "receiver";
    private static final String REGISTRY_NAME = "registry";
    private static final String ID_GEN_NAME = "id_gen";
    private static final int REGISTRY_SIZE = 10;
    private static final String TM_CONSUMER_NAME = "tm_consumer";

    private final ActorSystem system;
    private final ActorRef registry;
    private final Map<Integer, ActorRef> receivers;

    private UP(ActorSystem system, ActorRef registry) {
        this.system = system;
        this.registry = registry;
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
        ActorRef tmActor = newActor(system, TMActor.class, TM_ACTOR_NAME, registry);
        ActorRef idGenerator = newActor(system, IdGenerator.class, ID_GEN_NAME);
        createReceivers(1, idGenerator, tmActor);

        Proxy proxy = Proxy.createWith(this);

        doRecover(() -> {
            ActorRef consumerActor = newActor(system, TMConsumerActor.class, TM_CONSUMER_NAME, tmActor);
            system.scheduler().schedule(Duration.Zero(),
                    Duration.create(2000, TimeUnit.MILLISECONDS), consumerActor, new PollMsg(),
                    system.dispatcher(), ActorRef.noSender());

        });
        LoggerGlobal.log("ACTOR SYSTEM INITALIZED", this);


        return proxy;
    }

    public static void main(String[] args) {
        UP up = UP.create();
        up.start();
    }

    //factory method for UP
    public static UP create() {
        ActorSystem system = ActorSystem.create();
        ActorRef registryActor = newActor(system, RegistryActor.class, REGISTRY_NAME, REGISTRY_SIZE);
        return new UP(system, registryActor);
    }

    //recover procedure
    private void doRecover(Procedure afterRecover) {
        int size = receivers.size();

        DBConnection.processUnfinishedTransactions(trList -> {
            for(int i = 0; i < trList.size(); i++) {
                receivers.get(i % size)
                        .tell(new RecoverRequest(trList.get(i)), ActorRef.noSender());
            }
        }, afterRecover);
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
