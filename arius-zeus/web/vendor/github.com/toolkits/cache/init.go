package cache

import (
	"time"
)

func InitCache(host string, idle, max int, toc, tor, tow, defaultExpiration time.Duration) {
	Instance = NewRedisCache(host, idle, max, toc, tor, tow, defaultExpiration)
}

func InitMemoryCache(defaultExpiration time.Duration) {
	Instance = NewInMemoryCache(defaultExpiration)
}
