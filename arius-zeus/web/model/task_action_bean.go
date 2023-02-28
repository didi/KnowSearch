package model

type TaskAction struct {
	ID     int64  `gorm:"primary_key"`
	Action string `gorm:"action"`
	TS     int    `gorm:"ts"`
}

func (TaskAction) TableName() string {
	return "task_action"
}
