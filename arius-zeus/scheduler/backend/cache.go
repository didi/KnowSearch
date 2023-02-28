package backend

import (
	"fmt"
	"time"

	"github.com/toolkits/cache"
)

func InitCache() {
	cache.Instance = cache.NewInMemoryCache(time.Hour)
}

func taskMetaCacheKey(id int64) string {
	return fmt.Sprintf("/cache/task/meta/%d", id)
}
