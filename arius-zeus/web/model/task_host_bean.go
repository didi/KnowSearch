package model

type TaskHost struct {
	ID       int64  `gorm:"id"`
	Hostname string `gorm:"hostname"`
	Status   string `gorm:"status"`
	Stdout   string `gorm:"stdout"`
	Stderr   string `gorm:"stderr"`
}
