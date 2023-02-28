package config

import (
	"fmt"
	"os"
	"strings"
)

func Endpoint() (string, error) {
	port := strings.Split(G.Listen, ":")[1]
	hostname, err := os.Hostname()
	if err != nil {
		return "", err
	}

	return fmt.Sprintf("%s:%s", hostname, port), nil
}
