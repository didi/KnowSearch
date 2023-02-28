package backend

import (
	"zeus/scheduler/config"
)

// TasksOfScheduler 查询某个scheduler负责的任务ID列表
func TasksOfScheduler(scheduler string) ([]int64, error) {
	var ids []int64
	err := DB.Table("task_scheduler").Where("scheduler = ?", scheduler).Pluck("id", &ids).Error
	return ids, err
}

// TakeOverTask 替换某个任务的scheduler，通常是因为老的scheduler已经挂了
func TakeOverTask(id int64, pre, current string) (bool, error) {
	up := DB.Table("task_scheduler").Where("id = ? and scheduler = ?", id, pre).Update("scheduler", current)
	if err := up.Error; err != nil {
		return false, err
	}

	return up.RowsAffected > 0, nil
}

// OrphanTaskIds 查询还没有被分配的任务ID列表
func OrphanTaskIds() ([]int64, error) {
	var ids []int64
	err := DB.Table("task_scheduler").LogMode(false).Where("scheduler = ''").Pluck("id", &ids).Error
	return ids, err
}

// MyTask 查询我负责的任务ID列表
func MyTask() ([]int64, error) {
	endpoint, err := config.Endpoint()
	if err != nil {
		return nil, err
	}

	return TasksOfScheduler(endpoint)
}
