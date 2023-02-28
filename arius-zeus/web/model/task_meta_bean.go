package model

import (
	"fmt"
	"strings"
	"time"
)

type TaskMeta struct {
	ID        int64 `gorm:"primary_key"`
	Creator   string
	Created   int
	Keywords  string
	Batch     int
	Tolerance int
	Timeout   int
	Pause     string
	Script    string
	Args      string
	Account   string
}

func (TaskMeta) TableName() string {
	return "task_meta"
}

func (t *TaskMeta) Del() error {
	tx := DB.Begin()
	if tx.Error != nil {
		return fmt.Errorf("cannot begin transaction")
	}

	sql := fmt.Sprintf("DELETE FROM task_meta WHERE id=%d", t.ID)
	if err := tx.Exec(sql).Error; err != nil {
		tx.Rollback()
		return err
	}

	sql = fmt.Sprintf("DELETE FROM %s WHERE id=%d", tbl(t.ID), t.ID)
	if err := tx.Exec(sql).Error; err != nil {
		tx.Rollback()
		return err
	}

	if tx.Commit().Error != nil {
		return fmt.Errorf("cannot commit transaction")
	}

	return nil
}

func (t *TaskMeta) CleanFields() error {
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

func (t *TaskMeta) KillHost(hostname string) error {
	bean, err := TaskHostRepo.Get(t.ID, hostname)
	if !(bean.Status == "running" || bean.Status == "timeout") {
		return fmt.Errorf("current status is:%s, cannot kill", bean.Status)
	}

	now := time.Now().Unix()
	var count int64
	err = DB.Table("host_doing").Where("id=? and hostname=?", t.ID, hostname).Count(&count).Error
	if err != nil {
		return err
	}

	if count == 0 {
		err = DB.Exec("INSERT INTO host_doing(id,hostname,ts,action) VALUES(?,?,?,?)", t.ID, hostname, now, "kill").Error
	} else {
		err = DB.Exec("UPDATE host_doing SET ts=?, action='kill' WHERE id=? and hostname=? and action <> 'kill'", now, t.ID, hostname).Error
	}
	return err
}

func (t *TaskMeta) IgnoreHost(hostname string) error {
	sql := fmt.Sprintf("UPDATE %s SET status='ignored' WHERE id=%d and hostname='%s'", tbl(t.ID), t.ID, hostname)
	return DB.Exec(sql).Error
}

func (t *TaskMeta) RedoHost(hostname string) error {
	now := time.Now().Unix()
	var count int64
	err := DB.Table("host_doing").Where("id=? and hostname=?", t.ID, hostname).Count(&count).Error
	if err != nil {
		return err
	}

	if count == 0 {
		err = DB.Exec("INSERT INTO host_doing(id,hostname,ts,action) VALUES(?,?,?,?)", t.ID, hostname, now, "start").Error
	} else {
		err = DB.Exec("UPDATE host_doing SET ts=?, action='start' WHERE id=? and hostname=? and action <> 'start'", now, t.ID, hostname).Error
	}

	if err != nil {
		return err
	}

	sql := fmt.Sprintf("UPDATE %s SET status='running' WHERE id=%d and hostname='%s'", tbl(t.ID), t.ID, hostname)
	return DB.Exec(sql).Error
}

func (t *TaskMeta) Hosts() ([]string, error) {
	var hosts []string
	err := DB.Table(tbl(t.ID)).Where("id=?", t.ID).Order("ii").Pluck("hostname", &hosts).Error
	return hosts, err
}

// IsFinished 给前端页面展示用的
func (t *TaskMeta) IsFinished() bool {
	action, err := TaskActionRepo.Get(t.ID)
	if err != nil {
		panic(err)
	}

	return action == nil
}
