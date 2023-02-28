package model

import (
	"fmt"

	"zeus/web/utils"
)

func TplInsert(obj *Tpl, hostnames string) (int64, error) {
	var err error
	if err = obj.CleanFields(); err != nil {
		return 0, err
	}

	hosts := utils.ParseLines(hostnames)

	tx := DB.Begin()
	if tx.Error != nil {
		return 0, fmt.Errorf("cannot begin transaction")
	}

	if err = tx.Create(obj).Error; err != nil {
		tx.Rollback()
		return 0, err
	}

	id := obj.ID

	for _, host := range hosts {
		if err = tx.Exec("INSERT INTO tpl_host(id, hostname) VALUES(?, ?)", id, host).Error; err != nil {
			tx.Rollback()
			return 0, err
		}
	}

	if tx.Commit().Error != nil {
		return 0, fmt.Errorf("cannot commit transaction")
	}

	return id, nil
}

func TplGet(id int64) (*Tpl, error) {
	var objs []Tpl
	err := DB.Where("id=?", id).Find(&objs).Error
	if err != nil {
		return nil, err
	}

	if len(objs) == 0 {
		return nil, nil
	}

	return &objs[0], nil
}

func TplCount(gid int64) (int, error) {
	var count int
	err := DB.Model(&Tpl{}).Where("gid=?", gid).Count(&count).Error
	return count, err
}

func TplList(gid int64) ([]Tpl, error) {
	ret := []Tpl{}
	err := DB.Where("gid=?", gid).Order("keywords").Find(&ret).Error
	return ret, err
}

func TplUpdateGID(tplIds []int64, grpId int64) error {
	return DB.Exec("update tpl set gid=? where id in (?)", grpId, tplIds).Error
}
