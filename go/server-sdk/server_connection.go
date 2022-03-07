package mqtt

import (
	"bytes"
	"fmt"
	"net/url"
	"strconv"
	"time"

	"github.com/streadway/amqp"
)

type ChannelConfig struct {
	AccessKey  string
	SecretKey  string
	InstanceId string
	Domain     string
	Port       int16
}

type ServerConnection struct {
	connection *amqp.Connection
	stopped    bool
}

func (s *ServerConnection) stop() {
	s.stopped = true
	s.connection.Close()
}

func NewServerConnection(conf *ChannelConfig) (*ServerConnection, error) {
	connection, err := amqp.DialConfig(buildAmqpConfig(conf))
	if err != nil {
		return nil, err
	}
	fmt.Println("got Connection ...")
	s := &ServerConnection{
		connection: connection,
	}
	checkConnection(s, conf)
	return s, nil
}

func buildAmqpConfig(conf *ChannelConfig) (string, amqp.Config) {
	var buf bytes.Buffer

	userName := GetUserName(conf.AccessKey, conf.InstanceId)
	now := time.Now()
	currentMillis := strconv.FormatInt(now.UnixNano()/1000000, 10)
	password := GetPassword(conf.SecretKey, currentMillis)

	buf.WriteString("amqp://")
	buf.WriteString(userName)
	buf.WriteString(":")
	buf.WriteString(url.QueryEscape(password))
	buf.WriteString("@")
	buf.WriteString(conf.Domain)
	buf.WriteString(":")
	buf.WriteString(strconv.Itoa(int(conf.Port)))
	buf.WriteString("/MQTT")

	amqpURI := buf.String()

	prop := make(map[string]interface{})
	prop["signKey"] = currentMillis

	return amqpURI, amqp.Config{
		Heartbeat:  10 * time.Second,
		Locale:     "en_US",
		Properties: prop,
	}
}

func checkConnection(s *ServerConnection, conf *ChannelConfig) {
	go func() {
		for {
			if s.stopped {
				return
			}
			time.Sleep(5 * time.Second)
			if s.connection.IsClosed() {
				connection, err := amqp.DialConfig(buildAmqpConfig(conf))
				if err != nil {
					fmt.Printf("Fail reconnection %s\n", err)
				} else {
					s.connection = connection
					fmt.Printf("renew connection ... \n")
				}
			}
		}
	}()
}
