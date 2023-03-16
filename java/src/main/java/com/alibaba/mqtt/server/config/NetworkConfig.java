package com.alibaba.mqtt.server.config;

import static com.rabbitmq.client.ConnectionFactory.*;

public class NetworkConfig {
    protected long networkRecoveryInterval = DEFAULT_NETWORK_RECOVERY_INTERVAL;
    protected int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    protected int handshakeTimeout = DEFAULT_HANDSHAKE_TIMEOUT;

    protected int requestedHeartbeat = DEFAULT_HEARTBEAT;
    protected int shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;

    public long getNetworkRecoveryInterval() {
        return networkRecoveryInterval;
    }

    public void setNetworkRecoveryInterval(long networkRecoveryInterval) {
        this.networkRecoveryInterval = networkRecoveryInterval;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getHandshakeTimeout() {
        return handshakeTimeout;
    }

    public void setHandshakeTimeout(int handshakeTimeout) {
        this.handshakeTimeout = handshakeTimeout;
    }

    public int getRequestedHeartbeat() {
        return requestedHeartbeat;
    }

    public void setRequestedHeartbeat(int requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }

    public int getShutdownTimeout() {
        return shutdownTimeout;
    }

    public void setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }
}
