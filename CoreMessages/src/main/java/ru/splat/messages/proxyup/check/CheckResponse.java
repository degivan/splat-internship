package ru.splat.messages.proxyup.check;

import ru.splat.messages.proxyup.ProxyUPMessage;

/**
 * Answer for CheckRequest message.
 */
public class CheckResponse extends ProxyUPMessage {
    private final CheckResult checkResult;

    public CheckResponse(Integer userId, CheckResult checkResult) {
        super(userId);
        this.checkResult = checkResult;
    }

    public CheckResult getCheckResult() {
        return checkResult;
    }
}
