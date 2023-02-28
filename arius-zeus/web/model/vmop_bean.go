package model

type Vmop struct {
	ID       int64  `gorm:"primary_key"`
	Hostname string `gorm:"hostname"`
	Username string `gorm:"username"`
}

func (Vmop) TableName() string {
	return "vmop"
}
