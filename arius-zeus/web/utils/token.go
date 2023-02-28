package utils

import (
	"crypto/md5"
	"encoding/hex"
	"fmt"
	"time"

	"zeus/web/config"
)

func GenerateToken(username string) string {
	ts := time.Now().Unix()
	t := fmt.Sprintf("%x", ts)
	secretKey := username + "generateToken" + t + config.G.HTTP.Secret

	h := md5.New()
	h.Write([]byte(secretKey))
	token := hex.EncodeToString(h.Sum(nil))

	return token
}
