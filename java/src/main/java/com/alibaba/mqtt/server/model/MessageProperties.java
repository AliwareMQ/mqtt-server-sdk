package com.alibaba.mqtt.server.model;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.LongString;

import java.util.Map;

public class MessageProperties {
    private static final String PROPERTY_MQTT_SECOND_TOPIC = "mqttSecondTopic";
    private static final String PROPERTY_MQTT_PARENT_TOPIC = "mqttParentTopic";
    private static final String PROPERTY_MQTT_CLIENT = "clientId";

    private String firstTopic;
    private String secondTopic;
    private String clientId;

    public MessageProperties(AMQP.BasicProperties properties) {
        Map<String, Object> headers = properties.getHeaders();
        if (headers == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            LongString value = (entry.getValue() instanceof LongString) ? (LongString) entry.getValue() : null;
            if (value == null) {
                continue;
            }
            if (PROPERTY_MQTT_PARENT_TOPIC.equals(entry.getKey())) {
                firstTopic = value.toString();
            } else if (PROPERTY_MQTT_SECOND_TOPIC.equals(entry.getKey())) {
                secondTopic = value.toString();
            } else if (PROPERTY_MQTT_CLIENT.equals(entry.getKey())) {
                clientId = value.toString();
            }
        }
    }

    public String getFirstTopic() {
        return firstTopic;
    }

    public String getSecondTopic() {
        return secondTopic;
    }

    public String getClientId() {
        return clientId;
    }
}
