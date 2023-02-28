package model

import (
	"errors"
	"fmt"
)

func GrpCount(pid int64) (int, error) {
	var count int
	err := DB.Table("grp").Where("pid=?", pid).Count(&count).Error
	return count, err
}

func GrpList(pid int64) ([]Grp, error) {
	ret := []Grp{}
	err := DB.Where("pid=?", pid).Order("name").Find(&ret).Error
	return ret, err
}

func GrpGet(id int64) (*Grp, error) {
	var objs []Grp
	err := DB.Where("id=?", id).Find(&objs).Error
	if err != nil {
		return nil, err
	}

	if len(objs) == 0 {
		return nil, nil
	}

	return &objs[0], nil
}

func GrpSearch(pid int64, query string) ([]Grp, error) {
	ret := []Grp{}
	orm := DB.Where("pid=?", pid).Order("name")

	if query != "" {
		orm = orm.Where("name like ?", "%"+query+"%")
	}

	err := orm.Find(&ret).Error
	return ret, err
}

func GrpInsert(pid int64, name, users string) error {
	var cnt int
	err := DB.Table("grp").Where("pid=? and name=?", pid, name).Count(&cnt).Error
	if err != nil {
		return err
	}

	if cnt > 0 {
		return errors.New("grp name already exists")
	}

	return DB.Exec("INSERT INTO grp(pid, name, users) VALUES(?, ?, ?)", pid, name, users).Error
}

func GrpAll() ([]Grp, error) {
	var objs []Grp

	err := DB.Find(&objs).Error

	return objs, err
}

func GrpGetByNameAndPid(pid int64, name string) (Grp, error) {
	var objs []Grp
	err := DB.Where("pid = ? and name = ?", pid, name).Find(&objs).Error

	if err != nil {
		return Grp{}, err
	}

	if len(objs) != 1 {
		return Grp{}, fmt.Errorf("not found grp[name=%s] in pid[%d]", name, pid)
	}

	return objs[0], nil
}
