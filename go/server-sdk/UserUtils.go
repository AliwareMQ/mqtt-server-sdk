package mqtt

import (
	"bytes"
	"encoding/base64"
	"strconv"
)

const (
	ACCESS_FROM_USER = 0
	COLON            = ":"
)

func GetUserName(ak string, instanceId string) string {
	var buffer bytes.Buffer
	buffer.WriteString(strconv.Itoa(ACCESS_FROM_USER))
	buffer.WriteString(COLON)
	buffer.WriteString(instanceId)
	buffer.WriteString(COLON)
	buffer.WriteString(ak)
	return base64.StdEncoding.EncodeToString(buffer.Bytes())
}

func GetPassword(sk string, currentMillis string) string {
	var buffer bytes.Buffer
	buffer.Write(HmacSha1(sk, currentMillis))
	return base64.StdEncoding.EncodeToString(buffer.Bytes())
}
