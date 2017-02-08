package ru.splat.messages.proxyup;

/**
 * Identifies class as a message between Proxy and UP.
 */
public class ProxyUPMessage {
    private final Integer userId;

    public ProxyUPMessage(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}
