package model

import "fmt"

func InsertToken(username, token string) error {
	var count int
	err := DB.Table("user_token").Where("username=?", username).Count(&count).Error
	if err != nil {
		return err
	}

	if count > 0 {
		sql := fmt.Sprintf("UPDATE user_token SET token='%s' WHERE username='%s'", token, username)
		err = DB.Exec(sql).Error

		return err
	}

	sql := fmt.Sprintf("INSERT INTO user_token(username, token) VALUES('%s', '%s')", username, token)
	err = DB.Exec(sql).Error

	return err
}

func GetTokenByUsername(username string) (string, error) {
	var objs []UserToken

	err := DB.Table("user_token").Where("username=?", username).Find(&objs).Error
	if err != nil {
		return "", err
	}

	if len(objs) > 0 {
		return objs[0].Token, nil
	}

	return "", nil
}

func FindToken(token string) (string, error) {
	var objs []UserToken

	err := DB.Table("user_token").Where("token=?", token).Find(&objs).Error
	if err != nil {
		return "", err
	}

	if len(objs) > 0 {
		return objs[0].Username, nil
	}

	return "", nil
}
