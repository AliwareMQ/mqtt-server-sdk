package main

import (
	"bytes"
	"fmt"
	"time"

	mqtt "github.com/AliwareMQ/mqtt-server-sdk/go/server-sdk"
)

const (
	accessKey = "xxx"
	secretKey = "xxx"
	instanceId ="xxx"
	domain = "xxxx"
	port = 5672
	firstTopic = "xxx"
	gid = "xxxx"
)

type MessageProcessor struct{}

func (t *MessageProcessor) Process(msgId string, messageProperties *mqtt.MessageProperties, body []byte) error {
	fmt.Printf("recv:%s,%s \n", msgId, string(body))
	return nil
}

type StatusProcessor struct{}

func (s *StatusProcessor) Process(statusNotice *mqtt.StatusNotice) error {
	fmt.Printf("status: clientId=%s,channelId=%s,eventType=%s\n", statusNotice.ClientID, statusNotice.ChannelID, statusNotice.EventType)
	return nil
}

func main() {
	var serverProducer = &mqtt.ServerProducer{}
	var channelConfig = &mqtt.ChannelConfig{
		AccessKey:  accessKey,
		SecretKey:  secretKey,
		InstanceId: instanceId,
		Domain:     domain,
		Port:       port,
	}
	serverProducer.Start(channelConfig)

	var mqttTopic bytes.Buffer
	mqttTopic.WriteString(firstTopic)
	mqttTopic.WriteString("/t2")

	go func() {
		for i := 0; i < 100; i++ {
			// message only be received by MQTT client
			msgId, err := serverProducer.SendMessage(mqttTopic.String(), []byte("test"))
			if err != nil {
				fmt.Printf("send: %s\n", err)
			} else {
				fmt.Printf("send: %s\n", msgId)
			}
			time.Sleep(2 * time.Second)
		}
	}()

	var serverConsumer = &mqtt.ServerConsumer{}
	serverConsumer.Start(channelConfig)

	// only receive messages from MQTT client
	err2 := serverConsumer.SubscribeTopic(firstTopic, &MessageProcessor{})
	if err2 != nil {
		panic(err2)
	}

	err3 := serverConsumer.SubscribeStatus(gid, &StatusProcessor{})
	if err3 != nil {
		panic(err3)
	}

	// hang mock
	time.Sleep(time.Duration(10) * time.Minute)

}
