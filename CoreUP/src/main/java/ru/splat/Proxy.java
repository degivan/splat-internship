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
import java.util.stream.Collectors;

/**
 * Placeholder for Proxy.
 */
public class Proxy {
    private final UP up;

    private Proxy(UP up) {
        this.up = up;
    }

    public NewResponse sendNewRequest(BetInfo betInfo) throws Exception {
        ActorRef receiver = up.getReceiver(betInfo.getUserId());
        betInfo.setSelectionsId(betInfo.getBetOutcomes().stream().map(BetOutcome::getOutcomeId)
        .collect(Collectors.toSet()));
        NewRequest newRequest = new NewRequest(betInfo);
        Timeout timeout = Timeout.apply(10, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(receiver, newRequest, timeout);

        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Throwable {
                LoggerGlobal.log("Response for NewRequest received: " + o.toString());
            }
        },up.getSystem().dispatcher());

        return (NewResponse) Await.result(future, timeout.duration());

    }

    public CheckResult sendCheckRequest(Long transactionId, Integer userId) throws Exception {
        CheckRequest checkRequest = new CheckRequest(transactionId, userId);
        ActorRef receiver = up.getReceiver(userId);
        Timeout timeout = Timeout.apply(10, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(receiver, checkRequest, timeout);

        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Throwable {
                LoggerGlobal.log("Response for CheckRequest received: " + o.toString());
            }
        },up.getSystem().dispatcher());

        return ((CheckResponse)Await.result(future, timeout.duration()))
                .getCheckResult();
    }

    public static Proxy createWith(UP up) {
        return new Proxy(up);
    }
}
