package model

import (
	"errors"
	"log"

	"zeus/web/config"
)

func InitRoot() {
	user, err := UserGet("root")
	if err != nil {
		log.Fatalln("cannot query user[root]:", err)
	}

	if user != nil {
		return
	}

	err = UserInsert("root", config.CryptoPass(config.G.Password), 10)
	if err != nil {
		log.Fatalln("cannot insert user[root]:", err)
	}
}

func UserGet(username string) (*User, error) {
	var objs []User
	err := DB.Where("username=?", username).Find(&objs).Error
	if err != nil {
		return nil, err
	}

	if len(objs) == 0 {
		return nil, nil
	}

	return &objs[0], nil
}

func UserInsert(username, password string, role int) error {
	var cnt int
	err := DB.Table("user").Where("username=?", username).Count(&cnt).Error
	if err != nil {
		return err
	}

	if cnt > 0 {
		return errors.New("user name already exists")
	}

	return DB.Exec("INSERT INTO user(username, password, role) VALUES(?, ?, ?)", username, password, role).Error
}

func UserExists(username string) (bool, error) {
	var count int64
	err := DB.Table("user").Where("username=?", username).Count(&count).Error
	if err != nil {
		return false, err
	}

	if count == 1 {
		return true, nil
	}

	return false, nil
}

func UserList(query string, limit, offset int) ([]User, error) {
	var objs []User

	orm := DB.Limit(limit).Offset(offset)
	if query != "" {
		orm = orm.Where("username like ?", "%"+query+"%")
	}

	err := orm.Find(&objs).Error
	return objs, err
}

func UserCount(query string) (int64, error) {
	orm := DB.Model(&User{})
	if query != "" {
		orm = orm.Where("username like ?", "%"+query+"%")
	}

	var count int64
	err := orm.Count(&count).Error

	return count, err
}

func UserDel(username string) error {
	return DB.Exec("DELETE FROM user WHERE username=?", username).Error
}
