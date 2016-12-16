package ru.ifmo.splat.messages;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Type of message from Proxy to "UP".
 */
public class RequestInfo {
    private final CompletableFuture<RequestResult> completableResult;
    private final Long userId;
    private final Long bet;
    private final Map<Long,Set<Long>> eventsResults;

    public RequestInfo(CompletableFuture<RequestResult> result, Long userId, Long bet, Map<Long, Set<Long>> eventsResults) {
        this.completableResult = result;
        this.userId = userId;
        this.bet = bet;
        this.eventsResults = eventsResults;
    }


    public Long getBet() {
        return bet;
    }

    public Long getUserId() {
        return userId;
    }

    public CompletableFuture<RequestResult> getCompletableResult() {
        return completableResult;
    }

    public Map<Long, Set<Long>> getEventsResults() {
        return eventsResults;
    }

    public Set<Long> getEvents() {
        return eventsResults.keySet();
    }
}
