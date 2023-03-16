package com.alibaba.mqtt.server.config;

public class ProducerConfig {
    private long sendTimeoutMills = 5000;

    public long getSendTimeoutMills() {
        return sendTimeoutMills;
    }

    public void setSendTimeoutMills(long sendTimeoutMills) {
        this.sendTimeoutMills = sendTimeoutMills;
    }
}
