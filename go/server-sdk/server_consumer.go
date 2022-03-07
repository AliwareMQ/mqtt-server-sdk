package mqtt

import (
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/streadway/amqp"
)

const (
	PROPERTY_MQTT_SECOND_TOPIC = "mqttSecondTopic"
	PROPERTY_MQTT_PARENT_TOPIC = "mqttParentTopic"
	PROPERTY_MQTT_CLIENT       = "clientId"
)

type ServerConsumer struct {
	serverConnection       *ServerConnection
	channel                *amqp.Channel
	stopped                bool
	topicSubscribeHandler  map[string]MessageListener
	statusSubscribeHandler map[string]StatusListener
}

type MessageProperties struct {
	FirstTopic  string
	SecondTopic string
	ClientId    string
}

type StatusNotice struct {
	ChannelID string `json:"channelId"`
	ClientID  string `json:"clientId"`
	ClientIP  string `json:"clientIp"`
	EventType string `json:"eventType"`
	Time      int64  `json:"time"`
}

type MessageListener interface {
	Process(msgId string, messageProperties *MessageProperties, body []byte) error
}

type StatusListener interface {
	Process(*StatusNotice) error
}

func (serverConsumer *ServerConsumer) Start(channelConfig *ChannelConfig) {
	serverConnection, err := NewServerConnection(channelConfig)
	if err != nil {
		panic(err)
	}
	serverConsumer.serverConnection = serverConnection
	channel, err := serverConnection.connection.Channel()
	if err != nil {
		panic(err)
	}
	serverConsumer.channel = channel
	serverConsumer.stopped = false
	serverConsumer.topicSubscribeHandler = make(map[string]MessageListener)
	serverConsumer.statusSubscribeHandler = make(map[string]StatusListener)
	checkChannel(serverConsumer)
}

func checkChannel(serverConsumer *ServerConsumer) {
	if serverConsumer.stopped {
		return
	}
	channelCloseError := make(chan *amqp.Error)
	serverConsumer.channel.NotifyClose(channelCloseError)
	go func() {
		fmt.Printf("channelCloseError: %s\n", <-channelCloseError)
		for {
			channel, err := serverConsumer.serverConnection.connection.Channel()
			if err == nil {
				fmt.Printf("recreated channel ... \n")
				serverConsumer.channel = channel
				checkChannel(serverConsumer)
				err := recoverSubscribe(serverConsumer)
				if err != nil {
					fmt.Printf("Fail recoverSubscribe \n")
					time.Sleep(5 * time.Second)
					serverConsumer.channel.Close()
				}
				break
			} else {
				fmt.Printf("create channel failed %s\n", err)
				time.Sleep(5 * time.Second)
			}
		}
	}()
}

func recoverSubscribe(serverConsumer *ServerConsumer) error {
	for k, v := range serverConsumer.topicSubscribeHandler {
		err := serverConsumer._subscribeTopic(k, v)
		if err != nil {
			return err
		}
	}
	for k, v := range serverConsumer.statusSubscribeHandler {
		err := serverConsumer._subscribeStatus(k, v)
		if err != nil {
			return err
		}
	}
	return nil
}

func (serverConsumer *ServerConsumer) Stop() {
	serverConsumer.stopped = true
	serverConsumer.serverConnection.stop()
}

func (serverConsumer *ServerConsumer) SubscribeTopic(firstTopic string, messageListener MessageListener) error {
	serverConsumer.topicSubscribeHandler[firstTopic] = messageListener
	return serverConsumer._subscribeTopic(firstTopic, messageListener)
}

func (serverConsumer *ServerConsumer) _subscribeTopic(firstTopic string, messageListener MessageListener) error {
	channel := serverConsumer.channel
	consumerTag := strings.ReplaceAll(uuid.NewString(), "-", "")
	deliveries, err := channel.Consume(
		firstTopic,
		consumerTag,
		false, // noAck
		false, // exclusive
		false, // noLocal
		false, // noWait
		nil,   // arguments
	)
	if err != nil {
		return err
	}

	go func() {
		for d := range deliveries {
			var firstTopicValue string
			var secondTopicValue string
			var clientIdValue string
			if d.Headers[PROPERTY_MQTT_PARENT_TOPIC] != nil {
				firstTopicValue = d.Headers[PROPERTY_MQTT_PARENT_TOPIC].(string)
			}
			if d.Headers[PROPERTY_MQTT_SECOND_TOPIC] != nil {
				secondTopicValue = d.Headers[PROPERTY_MQTT_SECOND_TOPIC].(string)
			}
			if d.Headers[PROPERTY_MQTT_CLIENT] != nil {
				clientIdValue = d.Headers[PROPERTY_MQTT_CLIENT].(string)
			}
			err := messageListener.Process(d.MessageId,
				&MessageProperties{
					FirstTopic:  firstTopicValue,
					SecondTopic: secondTopicValue,
					ClientId:    clientIdValue,
				},
				d.Body)
			if err != nil {
				d.Nack(false, false)
			} else {
				d.Ack(false)
			}
		}
	}()

	return nil
}

func (serverConsumer *ServerConsumer) SubscribeStatus(mqttGroupId string, statusListener StatusListener) error {
	serverConsumer.statusSubscribeHandler[mqttGroupId] = statusListener
	return serverConsumer._subscribeStatus(mqttGroupId, statusListener)
}

func (serverConsumer *ServerConsumer) _subscribeStatus(mqttGroupId string, statusListener StatusListener) error {
	channel := serverConsumer.channel
	consumerTag := strings.ReplaceAll(uuid.NewString(), "-", "")
	arg := make(map[string]interface{})
	arg["GROUP_ID"] = mqttGroupId
	deliveries, err := channel.Consume(
		"STATUS",
		consumerTag,
		false, // noAck
		false, // exclusive
		false, // noLocal
		false, // noWait
		arg,   // arguments
	)
	if err != nil {
		return err
	}

	go func() {
		for d := range deliveries {
			var statusNotice StatusNotice
			err := json.Unmarshal(d.Body, &statusNotice)
			if err == nil {
				err := statusListener.Process(&statusNotice)
				if err != nil {
					d.Nack(false, false)
				} else {
					d.Ack(false)
				}
			} else {
				// ignore
				d.Ack(false)
			}
		}
	}()

	return nil
}
