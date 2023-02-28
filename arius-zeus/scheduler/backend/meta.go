package backend

import (
	"log"

	"github.com/toolkits/cache"
)

// TaskMeta 任务基本信息，一旦创建不会更改
// 一些可能会更改的字段，比如action，被抽取到其他表了
type TaskMeta struct {
	ID        int64 `gorm:"primary_key"`
	Creator   string
	Created   int
	Batch     int
	Tolerance int
	Timeout   int
	Pause     string
	Script    string
	Args      string
	Account   string
}

func (TaskMeta) TableName() string {
	return "task_meta"
}

// GetTaskMeta 从进程内存Cache中或者数据库获取TaskMeta
// 敢放到进程内存里，是因为TaskMeta是不会修改的，没有状态不一致的风险
func GetTaskMeta(id int64) (*TaskMeta, error) {
	var t TaskMeta
	if err := cache.Get(taskMetaCacheKey(id), &t); err == nil {
		return &t, nil
	}

	var ts []TaskMeta

	if err := DB.Where("id = ?", id).Find(&ts).Error; err != nil {
		log.Printf("[E] query task meta of id: %d fail %s\n", id, err.Error())
		return nil, err
	}

	if len(ts) > 0 {
		cache.Set(taskMetaCacheKey(id), ts[0], cache.DEFAULT)
		return &ts[0], nil
	}

	return nil, nil
}
