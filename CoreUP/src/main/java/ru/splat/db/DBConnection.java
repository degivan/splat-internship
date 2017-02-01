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
import com.mongodb.client.model.Projections;
import org.bson.Document;
import ru.splat.messages.Transaction;
import ru.splat.messages.proxyup.bet.BetInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.ne;
import static ru.splat.messages.Transaction.State.CREATED;

/**
 * Wrapper class for database.
 */
public class DBConnection {
    private static MongoDatabase db;
    private static MongoCollection<Document> transactions;
    private static final MongoCollection<Document> counter;
    private static final Document searchQuery;
    private static final Document updateQuery;
    private static ObjectMapper mapper;

    static {
        db = MongoClients.create()
                .getDatabase("test");
        transactions = db.getCollection("transactions");
        counter = db.getCollection("counter");
        searchQuery = Document.parse("{ _id : \"tr_id\" } ");
        updateQuery = Document.parse("{$inc: { \"seq\" : 1 } }");
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
    }

    /**
     * Put new transaction in database.
     * @param betInfo information about bet
     * @param after what to do with transaction after inserting
     */
    public static void newTransaction(BetInfo betInfo, Consumer<Transaction> after) {
        Transaction transaction = Transaction.statelessTransaction(betInfo);

        counter.findOneAndUpdate(searchQuery, updateQuery, (document, throwable) -> {
                Long trId = document.getLong("seq");
                transaction.setTransactionId(trId);
                transaction.setState(CREATED);
                insertNewTransaction(transaction, after);
        });
    }

    private static void insertNewTransaction(Transaction transaction, Consumer<Transaction> after) {
        try {
            transactions.insertOne(Document.parse(mapper.writeValueAsString(transaction)),
                    (aVoid, throwable) -> after.accept(transaction));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static FindIterable<Document> findUnfinishedTransactions() {
        return transactions.find(and(ne("state", "COMPLETED"), ne("state", "DENIED")))
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

    public static void resaveTransaction(Transaction transaction, Procedure procedure) {
        //TODO: resave transaction with a new state
    }
}
