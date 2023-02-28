package model

type TaskScheduler struct {
	ID        int64 `gorm:"primary_key"`
	Scheduler string
}

func (TaskScheduler) TableName() string {
	return "task_scheduler"
}
