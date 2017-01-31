package ru.splat;

import akka.actor.ActorRef;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ru.ifmo.splat.messages.proxyup.bet.BetInfo;
import ru.ifmo.splat.messages.proxyup.bet.NewRequest;
import ru.ifmo.splat.messages.proxyup.bet.NewResponse;
import ru.ifmo.splat.messages.proxyup.check.CheckRequest;
import ru.ifmo.splat.messages.proxyup.check.CheckResponse;
import ru.ifmo.splat.messages.proxyup.check.CheckResult;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

/**
 * Placeholder for Proxy.
 */
public class Proxy {
    private final UP up;

    private Proxy(UP up) {
        this.up = up;
    }

    public void sendNewRequest(BetInfo betInfo) {
        ActorRef receiver = up.getReceiver(betInfo.getUserId());
        NewRequest newRequest = new NewRequest(betInfo);

        Future<Object> future = Patterns.ask(receiver, newRequest, Timeout.apply(10, TimeUnit.SECONDS));

        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Throwable {
                NewResponse newResponse = (NewResponse) o;
                Long trId = newResponse.getTransactionId();
                //do something with transactionId
            }
        },up.getSystem().dispatcher());
    }

    public void sendCheckRequest(Long transactionId, Long userId) {
        CheckRequest checkRequest = new CheckRequest(transactionId, userId);
        ActorRef receiver = up.getReceiver(userId);

        Future<Object> future = Patterns.ask(receiver, checkRequest, Timeout.apply(10, TimeUnit.SECONDS));

        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object o) throws Throwable {
                CheckResponse checkResponse = (CheckResponse) o;
                CheckResult checkResult = checkResponse.getCheckResult();
                //do something with checkResult
            }
        },up.getSystem().dispatcher());
    }

    public static Proxy createWith(UP up) {
        return new Proxy(up);
    }
}
