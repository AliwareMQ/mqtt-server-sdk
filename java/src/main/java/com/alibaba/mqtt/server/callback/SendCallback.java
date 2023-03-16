package com.alibaba.mqtt.server.callback;

public interface SendCallback {
    void onSuccess(String msgId);

    void onFail();
}
