package ru.splat.db;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import ru.splat.LoggerGlobal;
import ru.splat.messages.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.ne;

/**
 * Wrapper class for database.
 */
public class DBConnection {
    private static MongoDatabase db;
    private static MongoCollection<Document> transactions;
    private static final MongoCollection<Document> counter;
    private static final Document searchIdBoundsQuery;
    private static final Document rangeQuery;
    private static ObjectMapper mapper;

    static {
        db = MongoClients.create()
                .getDatabase("test");
        transactions = db.getCollection("transactions");
        counter = db.getCollection("bounds");
        searchIdBoundsQuery = Document.parse("{ _id : \"tr_id\" } ");
        rangeQuery = Document.parse("{ $inc: { lower : 10000 , upper : 10000 } }");
        mapper = new ObjectMapper();
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
            transactions.insertOne(Document.parse(mapper.writeValueAsString(transaction)),
                    (aVoid, throwable) -> {
                        after.accept(transaction);

                        LoggerGlobal.log("New transaction in the database:"
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
            transactions.findOneAndReplace(Filters.eq("lower", transaction.getLowerBound()),
                    Document.parse(mapper.writeValueAsString(transaction)),
                    (o, throwable) -> after.process());

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

            LoggerGlobal.log("Indexes created from " + lower + " to " + upper);
        }));

    }

    private static FindIterable<Document> findUnfinishedTransactions() {
        return transactions.find(
                and(ne("state", "COMPLETED"),
                    ne("state", "DENIED")))
                .projection(Projections.excludeId());
    }

    private static Block<? super Document> processResult(List<Transaction> list) {
        return (Block<Document>) document -> list.add(getTransactionFromDocument(document));
    }

    private static SingleResultCallback<Void> createCallback(Consumer<List<Transaction>> processData, Procedure after,
                                                             List<Transaction> transactions) {
        return (aVoid, throwable) -> {
            processData.accept(transactions);
            after.process();
        };
    }

    private static Transaction getTransactionFromDocument(Document document) {
        try {
            return mapper.readValue(document.toJson(), Transaction.class);
        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeJsonMappingException("Document is in inappropriate state: " + document.toString());
        }
    }
}
