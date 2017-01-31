package ru.ifmo.splat.messages.proxyup.check;

import ru.ifmo.splat.messages.proxyup.ProxyUPMessage;

/**
 * Answer for CheckRequest message.
 */
public class CheckResponse extends ProxyUPMessage {
    private final CheckResult checkResult;

    public CheckResponse(Long userId, CheckResult checkResult) {
        super(userId);
        this.checkResult = checkResult;
    }

    public CheckResult getCheckResult() {
        return checkResult;
    }
}
