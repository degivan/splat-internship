package ru.splat.db;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.mongodb.Block;
import com.mongodb.ServerAddress;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.splat.LoggerGlobal;
import ru.splat.messages.Transaction;
import ru.splat.messages.uptm.trstate.TransactionState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.ne;
import static java.util.Collections.singletonList;

/**
 * Wrapper class for database.
 */
public class DBConnection {
    private static final MongoCollection<Document> states;
    private static final MongoCollection<Document> transactions;
    private static final MongoCollection<Document> counter;
    private static final Document searchIdBoundsQuery;
    private static final Document rangeQuery;
    private static final ObjectMapper MAPPER;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnection.class);


    static {
        ClusterSettings clusterSettings = ClusterSettings.builder()
                .hosts(singletonList(new ServerAddress("localhost", 27017)))
                .build();
        ConnectionPoolSettings poolSettings = ConnectionPoolSettings.builder()
                .maxSize(16)
                .build();
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .clusterSettings(clusterSettings)
                .connectionPoolSettings(poolSettings)
                .build();

        MongoDatabase db = MongoClients.create(clientSettings)
                .getDatabase("test");
        transactions = db.getCollection("transactions");
        states = db.getCollection("states");
        counter = db.getCollection("bounds");
        searchIdBoundsQuery = Document.parse("{ _id : \"tr_id\" } ");
        rangeQuery = Document.parse("{ $inc: { lower : 10000 , upper : 10000 } }");
        MAPPER = new ObjectMapper();
    }

    /**
     * Saves transactionState in the database.
     * @param trState TransactionState
     * @param after callback
     */
    public static void addTransactionState(TransactionState trState, Consumer<TransactionState> after) {
        try {
            states.replaceOne(byTransactionId(trState.getTransactionId()),
                    Document.parse(MAPPER.writeValueAsString(trState)),
                    new UpdateOptions().upsert(true),
                    (aVoid, throwable) -> {
                        after.accept(trState);

                        LOGGER.info(trState.toString() + " added to UP database.");
                    });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void findTransactionState(Long trId, Consumer<TransactionState> after) {
        states.find(byTransactionId(trId))
                .limit(1)
                .projection(Projections.excludeId())
                .forEach(document -> {
                            TransactionState tState = getObjectFromDocument(document,TransactionState.class);
                            LOGGER.info(tState.toString() + " finded in the database.");

                            after.accept(tState);
                        },
                        (result, t) -> {});
    }

    public static void getTransactionStates(Consumer<List<TransactionState>> after) {
        List<TransactionState> trStates = new ArrayList<>();

        states.find()
                .projection(Projections.excludeId())
                .forEach(document -> {
                    TransactionState tState = getObjectFromDocument(document, TransactionState.class);
                    LoggerGlobal.log(tState.toString() + " finded in the database.");

                    trStates.add(tState);
                }, (result, t) -> after.accept(trStates));
    }


    /**
     * Finds all unfinished transactions and work with them.
     * @param processData process list of transactions
     * @param after make some action after list processing finished
     */
    public static void processUnfinishedTransactions(Consumer<List<Transaction>> processData, Procedure after) {
        List<Transaction> list = new ArrayList<>();
        findUnfinishedTransactions()
                .forEach(processResult(list), createCallback(processData, after, list));

        LoggerGlobal.log("Unfinished transactions sended.");
    }

    /**
     * Put new transaction in database.
     * @param transaction information about bet
     * @param after what to do with transaction after inserting
     */
    public static void newTransaction(Transaction transaction, Consumer<Transaction> after) {
        try {
            transactions.insertOne(Document.parse(MAPPER.writeValueAsString(transaction)),
                    (aVoid, throwable) -> {
                        after.accept(transaction);

                       LOGGER.info("New transaction in the database:"
                                + transaction.toString());
                    });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overwrites existing transaction in database
     * @param transaction transaction to overwrite
     * @param after action after overwriting
     */
    public static void overwriteTransaction(Transaction transaction, Procedure after) {
        try {
            transactions.findOneAndReplace(Filters.eq("lowerBound", transaction.getLowerBound()),
                    Document.parse(MAPPER.writeValueAsString(transaction)),
                    (o, throwable) -> {
                        LOGGER.info(transaction.toString() + "is overwrited.");
                        after.process();
                    });

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create new identifiers for transactions.
     * @param after processing transactions after creating
     */
    public static void createIdentifiers(Consumer<Bounds> after) {
        counter.findOneAndUpdate(searchIdBoundsQuery, rangeQuery, ((document, throwable) -> {
            Long lower = document.getLong("lower");
            Long upper = document.getLong("upper");

            after.accept(new Bounds(lower, upper));

            LOGGER.info("Indexes created from " + lower + " to " + upper);
        }));

    }

    @NotNull
    private static Bson byTransactionId(Long trId) {
        return Filters.eq("transactionId", trId);
    }

    private static FindIterable<Document> findUnfinishedTransactions() {
        return transactions.find(
                and(ne("state", "COMPLETED"),
                    ne("state", "CANCEL_COMPLETED")))
                .projection(Projections.excludeId());
    }

    private static Block<? super Document> processResult(List<Transaction> list) {
        return (Block<Document>) document -> list.add(getObjectFromDocument(document, Transaction.class));
    }

    private static SingleResultCallback<Void> createCallback(Consumer<List<Transaction>> processData, Procedure after,
                                                             List<Transaction> transactions) {
        return (aVoid, throwable) -> {
            processData.accept(transactions);
            after.process();
        };
    }

    private static <T>  T getObjectFromDocument(Document document, Class<T> clazz ) {
         try {
             return MAPPER.readValue(document.toJson(), clazz);
         } catch(IOException e) {
             e.printStackTrace();
             throw new RuntimeJsonMappingException("Document is in inappropriate state: " + document.toString());
         }
    }

}
