package model

type UserToken struct {
	Username string `gorm:"username"`
	Token    string `gorm:"token"`
}

func (UserToken) TableName() string {
	return "user_token"
}
