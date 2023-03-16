package com.alibaba.mqtt.server.callback;


import com.alibaba.mqtt.server.model.MessageProperties;

public interface MessageListener {
    void process(String msgId, MessageProperties messageProperties, byte[] payload);
}
