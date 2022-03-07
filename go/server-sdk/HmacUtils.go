package mqtt

import (
	"crypto/hmac"
	"crypto/sha1"
)

func HmacSha1(keyStr string, message string) []byte {
	key := []byte(keyStr)
	mac := hmac.New(sha1.New, key)
	mac.Write([]byte(message))
	return mac.Sum(nil)
}
