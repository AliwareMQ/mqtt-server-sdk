package mqtt

import (
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/streadway/amqp"
)

const (
	ChannelNum = 16
)

type ServerProducer struct {
	serverConnection *ServerConnection
	sendChannels     [ChannelNum]*SendChannel
	rid              uint64
	stopped          bool
}

type SendChannel struct {
	channel  *amqp.Channel
	confirms chan amqp.Confirmation
}

func (serverProducer *ServerProducer) Start(channelConfig *ChannelConfig) {
	serverConnection, err := NewServerConnection(channelConfig)
	if err != nil {
		panic(err)
	}
	serverProducer.serverConnection = serverConnection
	serverProducer.stopped = false
	initSendChannels(serverProducer)
	checkSendChannels(serverProducer)
}

func checkSendChannels(serverProducer *ServerProducer) {
	go func() {
		for {
			if serverProducer.stopped {
				return
			}
			time.Sleep(5 * time.Second)
			initSendChannels(serverProducer)
		}
	}()
}

func initSendChannels(serverProducer *ServerProducer) {
	for i := 0; i < ChannelNum; i++ {
		sendChannel := serverProducer.sendChannels[i]
		if sendChannel == nil {
			connection := serverProducer.serverConnection.connection
			channel, err := connection.Channel()
			if err != nil {
				fmt.Printf("Fail to New Channel %s \n", err)
				break
			}
			fmt.Printf("new Channel-[%d] \n", i)
			serverProducer.sendChannels[i], err = buildSendChannel(channel)
			if err != nil {
				fmt.Printf("Fail to buildSendChannel %s \n", err)
				break
			}
			channelCloseError := make(chan *amqp.Error)
			channel.NotifyClose(channelCloseError)
			finalIndex := i
			go func() {
				fmt.Printf("channelCloseError: %s\n", <-channelCloseError)
				serverProducer.sendChannels[finalIndex] = nil
			}()
		}
	}
}

func buildSendChannel(channel *amqp.Channel) (*SendChannel, error) {
	if err := channel.Confirm(false); err != nil {
		return nil, err
	}
	confirms := channel.NotifyPublish(make(chan amqp.Confirmation, 1))
	return &SendChannel{
		channel:  channel,
		confirms: confirms,
	}, nil
}

func (serverProducer *ServerProducer) Stop() {
	serverProducer.serverConnection.stop()
}

func (serverProducer *ServerProducer) SendMessage(mqttTopic string, body []byte) (string, error) {
	var channel *amqp.Channel
	var confirms chan amqp.Confirmation
	var i uint64
	for j := 0; j < ChannelNum; j++ {
		serverProducer.rid++
		i = serverProducer.rid % ChannelNum
		sendChannel := serverProducer.sendChannels[i]
		if sendChannel != nil {
			channel = sendChannel.channel
			confirms = sendChannel.confirms
			break
		}
	}
	if channel == nil {
		return "", errors.New("NoChannel")
	}
	messageId := strings.ReplaceAll(uuid.NewString(), "-", "")
	err := channel.Publish(
		mqttTopic,
		mqttTopic,
		true,
		false,
		amqp.Publishing{
			Headers:      amqp.Table{},
			MessageId:    messageId,
			Body:         []byte(body),
			DeliveryMode: amqp.Persistent,
			Priority:     0,
		},
	)
	if err != nil {
		return "", err
	}
	select {
	case confirmed := <-confirms:
		if confirmed.Ack {
			return messageId, nil
		} else {
			return "", errors.New("nack")
		}
	case <-time.After(5000 * time.Millisecond):
		// maybe lost ack/nack
		serverProducer.sendChannels[i] = nil
		channel.Close()

		return "", errors.New("timeout")
	}

}
