package ru.ifmo.splat.messages.proxyup;

/**
 * Identifies class as a message between Proxy and UP.
 */
public class ProxyUPMessage {
    private final Long userId;

    public ProxyUPMessage(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
