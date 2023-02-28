package model

import (
	"fmt"
	"time"
)

var TaskActionRepo = new(TaskAction)

func (*TaskAction) Get(id int64) (*TaskAction, error) {
	as := []TaskAction{}
	err := DB.Table("task_action").Where("id=?", id).Find(&as).Error
	if err != nil {
		return nil, err
	}

	if len(as) > 0 {
		return &as[0], nil
	}

	return nil, nil
}

// Update 更新任务指令
// web上用户的操作如何让scheduler感知呢？比如原来是pause，现在start了
// 可以把task_scheduler表中当前这个task id对应的scheduler清空，这样scheduler就会去抢夺这个task
func (*TaskAction) Update(id int64, action string) error {
	err := DB.Exec("UPDATE task_action SET action=? WHERE id = ?", action, id).Error
	if err != nil {
		return err
	}

	if action == "start" {
		return DB.Exec("UPDATE task_scheduler SET scheduler='' WHERE id=?", id).Error
	}

	if action == "cancel" {
		return CancelWaitingHosts(id)
	}

	if action == "kill" {
		err = CancelWaitingHosts(id)
		if err != nil {
			return err
		}
		return KillTask(id)
	}

	return nil
}

// CancelWaitingHosts 放弃执行还未执行的机器
func CancelWaitingHosts(id int64) error {
	sql := fmt.Sprintf("UPDATE %s SET status = 'cancelled' WHERE id = %d and status = 'waiting'", tbl(id), id)
	return DB.Exec(sql).Error
}

// KillTask 下发kill指令
// action <> 'kill' 任务不会重复kill，没意义
func KillTask(id int64) error {
	now := time.Now().Unix()
	tx := DB.Begin()
	if err := tx.Error; err != nil {
		return err
	}

	if err := tx.Exec("UPDATE host_doing SET ts=?, action='kill' WHERE id=? and action <> 'kill'", now, id).Error; err != nil {
		return err
	}

	if err := tx.Exec(fmt.Sprintf("UPDATE %s SET status = 'killing' WHERE id=%d and status='running'", tbl(id), id)).Error; err != nil {
		return err
	}

	return tx.Commit().Error
}

// LongTaskIDs 执行超过14天的任务
func LongTaskIDs() ([]int64, error) {
	ts := time.Now().Unix() - 604800*2
	var ids []int64
	err := DB.Table("task_action").Where("ts < ?", ts).Pluck("id", &ids).Error
	return ids, err
}
