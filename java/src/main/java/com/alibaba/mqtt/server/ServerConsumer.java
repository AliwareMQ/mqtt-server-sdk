package com.alibaba.mqtt.server;

import com.alibaba.mqtt.server.callback.MessageListener;
import com.alibaba.mqtt.server.callback.StatusListener;
import com.alibaba.mqtt.server.config.ChannelConfig;
import com.alibaba.mqtt.server.config.ConsumerConfig;
import com.alibaba.mqtt.server.model.MessageProperties;
import com.alibaba.mqtt.server.model.StatusNotice;
import com.alibaba.mqtt.server.network.AbstractChannel;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ServerConsumer extends AbstractChannel {
    private static final int CONNECTION_NUM = 4;
    private Connection[] connections = new Connection[CONNECTION_NUM];
    private Channel[] channels = new Channel[CONNECTION_NUM];
    private ConsumerConfig consumerConfig;
    private ExecutorService msgExecutor;
    private ExecutorService statusExecutor;

    public ServerConsumer(ChannelConfig channelConfig, ConsumerConfig consumerConfig) {
        super(channelConfig);
        this.consumerConfig = consumerConfig;
    }

    public void start() throws IOException, TimeoutException {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        super.start();
        for (int i = 0; i < CONNECTION_NUM; i++) {
            connections[i] = factory.newConnection();
            channels[i] = connections[i].createChannel();
        }
        msgExecutor = new ThreadPoolExecutor(
                consumerConfig.getMinConsumeThreadNum(),
                consumerConfig.getMaxConsumeThreadNum(),
                1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(30000));

        statusExecutor = new ThreadPoolExecutor(
                consumerConfig.getMinConsumeThreadNum(),
                consumerConfig.getMaxConsumeThreadNum(),
                1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(30000));
    }

    public void stop() throws IOException {
        for (int i = 0; i < CONNECTION_NUM; i++) {
            connections[i].close();
        }
    }

    public void subscribeTopic(String firstTopic, MessageListener messageListener) throws IOException {
        for (int i = 0; i < CONNECTION_NUM; i++) {
            Channel channel = channels[i];
            channel.basicConsume(firstTopic, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) {
                    msgExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                messageListener.process(properties.getMessageId(), new MessageProperties(properties), body);
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (Throwable t) {
                                try {
                                    channel.basicNack(envelope.getDeliveryTag(), false, false);
                                } catch (IOException e) {
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    public void subscribeStatus(String mqttGroupId, StatusListener statusListener) throws IOException {
        Map<String, Object> arguments = new HashMap<>(4);
        arguments.put("GROUP_ID", mqttGroupId);
        for (int i = 0; i < CONNECTION_NUM; i++) {
            Channel channel = channels[i];
            channel.basicConsume("STATUS", false, arguments, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    statusExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                statusListener.process(new StatusNotice(body));
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (Throwable t) {
                                try {
                                    channel.basicNack(envelope.getDeliveryTag(), false, false);
                                } catch (IOException e) {
                                }
                            }
                        }
                    });
                }
            });
        }
    }

}
