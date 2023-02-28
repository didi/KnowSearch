package model

type User struct {
	ID       int64  `gorm:"primary_key" json:"id"`
	Username string `json:"username"`
	Password string `json:"-"`
	Role     int    `json:"role"`
}

func (User) TableName() string {
	return "user"
}
