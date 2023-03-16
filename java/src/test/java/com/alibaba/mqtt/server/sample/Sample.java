package com.alibaba.mqtt.server.sample;

import com.alibaba.mqtt.server.ServerConsumer;
import com.alibaba.mqtt.server.ServerProducer;
import com.alibaba.mqtt.server.callback.MessageListener;
import com.alibaba.mqtt.server.callback.SendCallback;
import com.alibaba.mqtt.server.callback.StatusListener;
import com.alibaba.mqtt.server.config.ChannelConfig;
import com.alibaba.mqtt.server.config.ConsumerConfig;
import com.alibaba.mqtt.server.config.ProducerConfig;
import com.alibaba.mqtt.server.model.MessageProperties;
import com.alibaba.mqtt.server.model.StatusNotice;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Sample {

    @Test
    public void test() throws InterruptedException, IOException, TimeoutException {
        String domain = System.getProperty("domain");
        int port = Integer.parseInt(System.getProperty("port"));
        String instanceId = System.getProperty("instanceId");
        String accessKey = System.getProperty("accessKey");
        String secretKey = System.getProperty("secretKey");

        String firstTopic = System.getProperty("firstTopic");
        String secondTopic = System.getProperty("secondTopic");
        String mqttGroupId = System.getProperty("mqttGroupId");

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.setDomain(domain);
        channelConfig.setPort(port);
        channelConfig.setInstanceId(instanceId);
        channelConfig.setAccessKey(accessKey);
        channelConfig.setSecretKey(secretKey);

        ServerConsumer serverConsumer = new ServerConsumer(channelConfig, new ConsumerConfig());
        serverConsumer.start();
        serverConsumer.subscribeTopic(firstTopic, new MessageListener() {
            @Override
            public void process(String msgId, MessageProperties messageProperties, byte[] payload) {
                System.out.println("Receive:" + msgId + "," + new String(payload));
            }
        });

        serverConsumer.subscribeStatus(mqttGroupId, new StatusListener() {
            @Override
            public void process(StatusNotice statusNotice) {
                System.out.println(statusNotice.getClientId() + "," + statusNotice.getStatusType());
            }
        });

        ServerProducer serverProducer = new ServerProducer(channelConfig, new ProducerConfig());
        serverProducer.start();
        for (int i = 0; i < 10000; i++) {
            Thread.sleep(1000);
            String mqttTopic = firstTopic + "/" + secondTopic;
            serverProducer.sendMessage(mqttTopic, ("hello " + i).getBytes(StandardCharsets.UTF_8), new SendCallback() {
                @Override
                public void onSuccess(String msgId) {
                    System.out.println("SendSuccess " + msgId);
                }

                @Override
                public void onFail() {
                    System.out.println("SendFail ");
                }
            });
        }
    }
}
