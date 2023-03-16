package com.alibaba.mqtt.server.network;

import com.alibaba.mqtt.server.config.ChannelConfig;
import com.alibaba.mqtt.server.util.AliyunCredentialsProvider;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractChannel {
    protected ConnectionFactory factory = new ConnectionFactory();
    private ChannelConfig channelConfig;
    protected AtomicBoolean started = new AtomicBoolean(false);

    public AbstractChannel(ChannelConfig channelConfig) {
        this.channelConfig = channelConfig;
    }

    protected void start() throws IOException, TimeoutException {
        long ts = System.currentTimeMillis();
        factory.setHost(channelConfig.getDomain());
        factory.setCredentialsProvider(
                new AliyunCredentialsProvider(channelConfig.getAccessKey(), channelConfig.getSecretKey(), channelConfig.getInstanceId(), ts));
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(channelConfig.getNetworkRecoveryInterval());
        factory.setPort(channelConfig.getPort());
        factory.setVirtualHost("MQTT");
        factory.setConnectionTimeout(channelConfig.getConnectionTimeout());
        factory.setHandshakeTimeout(channelConfig.getHandshakeTimeout());
        factory.setRequestedHeartbeat(channelConfig.getRequestedHeartbeat());
        factory.setShutdownTimeout(channelConfig.getShutdownTimeout());

        Map<String, Object> properties = new HashMap<>(1);
        properties.put("signKey", String.valueOf(ts));
        factory.setClientProperties(properties);
    }

}
