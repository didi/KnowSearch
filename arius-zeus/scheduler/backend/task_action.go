package backend

import (
	"time"
)

// TaskAction 用户对任务的操作指令，默认是start
type TaskAction struct {
	ID     int64  `gorm:"primary_key"`
	Action string `gorm:"action"`
	TS     int    `gorm:"column:ts"`
}

func (TaskAction) TableName() string {
	return "task_action"
}

func GetTaskAction(id int64) (*TaskAction, error) {
	var actions []TaskAction
	err := DB.Where("id = ?", id).Find(&actions).Error
	if err != nil {
		return nil, err
	}

	if len(actions) > 0 {
		return &actions[0], nil
	}

	return nil, nil
}

// UpdateAction 设置新的任务指令
func UpdateAction(id int64, action string) error {
	return DB.Exec("UPDATE task_action SET action=?, ts=? WHERE id=?", action, time.Now().Unix(), id).Error
}
