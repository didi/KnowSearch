package backend

import (
	"fmt"
	"time"

	"github.com/toolkits/cache"

	"zeus/common/model"
)

type TaskMeta struct {
	ID      int64
	Script  string
	Args    string
	Account string
}

func GetTaskMeta(id int64) (*TaskMeta, error) {
	key := taskMetaCacheKey(id)
	meta := new(TaskMeta)
	if err := cache.Get(key, meta); err == nil {
		return meta, nil
	}

	var tmr model.TaskMetaResponse
	if err := SchedulerClients.Call("Scheduler.GetTaskMeta", id, &tmr, time.Duration(5)*time.Second); err != nil {
		return nil, err
	}

	if tmr.Message != "" {
		return nil, fmt.Errorf(tmr.Message)
	}

	meta.ID = id
	meta.Script = tmr.Script
	meta.Args = tmr.Args
	meta.Account = tmr.Account

	cache.Set(key, *meta, cache.DEFAULT)
	return meta, nil
}
