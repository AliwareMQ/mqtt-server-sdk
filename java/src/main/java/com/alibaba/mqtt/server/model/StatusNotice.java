package com.alibaba.mqtt.server.model;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;

public class StatusNotice {
    private String clientId;
    private StatusType statusType;
    private String channelId;
    private String clientIp;
    private long time;
    private String certificateChainSn;

    public StatusNotice(byte[] payload) {
        JSONObject msgBody = JSON.parseObject(new String(payload, StandardCharsets.UTF_8));
        String eventType = msgBody.getString("eventType");
        if ("connect".equals(eventType)) {
            statusType = StatusType.ONLINE;
        } else {
            statusType = StatusType.OFFLINE;
        }
        this.clientId = msgBody.getString("clientId");
        this.channelId = msgBody.getString("channelId");
        this.clientIp = msgBody.getString("clientIp");
        this.time = msgBody.getLongValue("time");
        this.certificateChainSn = msgBody.getString("certificateChainSn");
    }

    public String getClientId() {
        return clientId;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public long getTime() {
        return time;
    }

    public String getCertificateChainSn() {
        return certificateChainSn;
    }
}
