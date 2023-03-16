package com.alibaba.mqtt.server.callback;


import com.alibaba.mqtt.server.model.StatusNotice;

public interface StatusListener {
    void process(StatusNotice statusNotice);
}
