package model

import (
	"fmt"
	"strings"

	"zeus/web/utils"
)

type Tpl struct {
	ID        int64  `gorm:"primary_key" json:"id"`
	GID       int64  `gorm:"column:gid" json:"gid"`
	Updator   string `json:"updator"`
	Updated   int    `json:"updated"`
	Keywords  string `json:"keywords"`
	Batch     int    `json:"batch"`
	Tolerance int    `json:"tolerance"`
	Timeout   int    `json:"timeout"`
	Pause     string `json:"pause"`
	Script    string `json:"script"`
	Args      string `json:"args"`
	Account   string `json:"account"`
}

func (Tpl) TableName() string {
	return "tpl"
}

func (t *Tpl) HasPriv(username string) (bool, error) {
	g, err := GrpGet(t.GID)
	if err != nil {
		return false, err
	}

	return g.HasPriv(username)
}

func (t *Tpl) CleanFields() error {
	if t.Batch < 0 {
		return fmt.Errorf("batch should be nonnegative")
	}

	if t.Tolerance < 0 {
		return fmt.Errorf("tolerance should be nonnegative")
	}

	if t.Timeout < 0 {
		return fmt.Errorf("timeout should be nonnegative")
	}

	if t.Timeout > 3600*24 {
		return fmt.Errorf("timeout longer than one day")
	}

	t.Pause = strings.Replace(t.Pause, "，", ",", -1)
	t.Pause = strings.Replace(t.Pause, " ", "", -1)
	t.Args = strings.Replace(t.Args, "，", ",", -1)

	if strings.Contains(t.Args, "'") || strings.Contains(t.Args, "\"") {
		return fmt.Errorf("args cannot contain ' or \"")
	}

	return nil
}

func (t *Tpl) Update(target *Tpl, hostnames string) error {
	err := target.CleanFields()
	if err != nil {
		return err
	}

	hosts := utils.ParseLines(hostnames)

	tx := DB.Begin()
	if tx.Error != nil {
		return fmt.Errorf("cannot begin transaction")
	}

	if err = tx.Exec("DELETE FROM tpl_host WHERE id=?", t.ID).Error; err != nil {
		tx.Rollback()
		return err
	}

	for _, host := range hosts {
		if err = tx.Exec("INSERT INTO tpl_host(id, hostname) VALUES(?, ?)", t.ID, host).Error; err != nil {
			tx.Rollback()
			return err
		}
	}

	t.Keywords = target.Keywords
	t.Batch = target.Batch
	t.Tolerance = target.Tolerance
	t.Timeout = target.Timeout
	t.Pause = target.Pause
	t.Script = target.Script
	t.Args = target.Args
	t.Account = target.Account
	t.Updator = target.Updator
	t.Updated = target.Updated

	if err = tx.Save(t).Error; err != nil {
		tx.Rollback()
		return err
	}

	if tx.Commit().Error != nil {
		return fmt.Errorf("cannot commit transaction")
	}

	return nil
}

func (t *Tpl) Del() error {
	tx := DB.Begin()
	if tx.Error != nil {
		return fmt.Errorf("cannot begin transaction")
	}

	if err := tx.Exec("DELETE FROM tpl WHERE id=?", t.ID).Error; err != nil {
		tx.Rollback()
		return err
	}

	if err := tx.Exec("DELETE FROM tpl_host WHERE id=?", t.ID).Error; err != nil {
		tx.Rollback()
		return err
	}

	if tx.Commit().Error != nil {
		return fmt.Errorf("cannot commit transaction")
	}

	return nil
}

// Hosts 获取模板的机器列表，记得排序，这很重要
func (t *Tpl) Hosts() ([]string, error) {
	var hosts []string
	err := DB.Table("tpl_host").Where("id=?", t.ID).Order("ii").Pluck("hostname", &hosts).Error
	return hosts, err
}
