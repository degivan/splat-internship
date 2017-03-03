package ru.splat;

import akka.actor.ActorRef;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ru.splat.messages.proxyup.bet.BetInfo;
import ru.splat.messages.proxyup.bet.NewRequest;
import ru.splat.messages.proxyup.bet.NewResponse;
import ru.splat.messages.proxyup.check.CheckRequest;
import ru.splat.messages.proxyup.check.CheckResponse;
import ru.splat.messages.proxyup.check.CheckResult;
import ru.splat.messages.uptm.trmetadata.bet.BetOutcome;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Placeholder for Proxy.
 */
public class Proxy {
    private final static Timeout TIMEOUT = Timeout.apply(10, TimeUnit.SECONDS);

    private final UP up;

    private Proxy(UP up) {
        this.up = up;
    }

    public NewResponse sendNewRequest(BetInfo betInfo) throws Exception {
        betInfo.setSelectionsId(betInfo.getBetOutcomes().stream().map(BetOutcome::getOutcomeId)
                .collect(Collectors.toSet()));
        NewRequest newRequest = new NewRequest(betInfo);

        return (NewResponse) getRequestResult(betInfo.getUserId(), newRequest,
                m -> "Response for NewRequest received: " + m.toString());
    }

    public CheckResult sendCheckRequest(Long transactionId, Integer userId) throws Exception {
        CheckRequest checkRequest = new CheckRequest(transactionId, userId);

        CheckResponse response =  (CheckResponse) getRequestResult(userId, checkRequest,
                m -> "Response for CheckRequest received: " + m.toString());

        return response.getCheckResult();
    }

    private Object getRequestResult(Integer userId, Object request,
                                    Function<Object, String> logBuilder) throws Exception {
        Future<Object> future = makeRequest(userId, request);

        logOnSuccess(future, logBuilder);

        return Await.result(future, TIMEOUT.duration());
    }

    private Future<Object> makeRequest(Integer userId, Object request) {
        ActorRef receiver = up.getReceiver(userId);
        return Patterns.ask(receiver, request, TIMEOUT);
    }

    private void logOnSuccess(Future<Object> future, Function<Object, String> logBuilder) {
        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Throwable {
                LoggerGlobal.log(logBuilder.apply(o), this);
            }
        },up.getSystem().dispatcher());
    }

    static Proxy createWith(UP up) {
        return new Proxy(up);
    }
}
