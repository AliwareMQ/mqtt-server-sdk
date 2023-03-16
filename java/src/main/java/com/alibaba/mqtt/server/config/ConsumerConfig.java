package com.alibaba.mqtt.server.config;

public class ConsumerConfig {
    private int minConsumeThreadNum = 16;
    private int maxConsumeThreadNum = 32;

    public int getMinConsumeThreadNum() {
        return minConsumeThreadNum;
    }

    public void setMinConsumeThreadNum(int minConsumeThreadNum) {
        this.minConsumeThreadNum = minConsumeThreadNum;
    }

    public int getMaxConsumeThreadNum() {
        return maxConsumeThreadNum;
    }

    public void setMaxConsumeThreadNum(int maxConsumeThreadNum) {
        this.maxConsumeThreadNum = maxConsumeThreadNum;
    }
}
