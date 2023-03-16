package com.alibaba.mqtt.server.common;

public class SendResult {
    private String msgId;
    private boolean success;

    public SendResult(boolean success) {
        this.success = success;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
