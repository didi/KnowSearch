package backend

import (
	"fmt"
	"zeus/scheduler/config"
)

type TaskHost struct {
	ID       int64  `gorm:"column:id"`
	Hostname string `gorm:"hostname"`
	TS       int    `gorm:"column:ts"`
	Status   string `gorm:"status"`
}

// MarkDoneStatus 任务在某个机器执行完成之后，在数据库中更新状态
func MarkDoneStatus(id int64, ts int, hostname, status, stdout, stderr string) error {
	// 先查一下这个ts的这个task有没有，没有，可能是redo或者kill了，即下发了新的action，老的那个action的结果就不关注了
	var count int
	err := DB.Table("host_doing").Where("id=? and hostname=? and ts=?", id, hostname, ts).Count(&count).Error
	if err != nil {
		return err
	}

	if count == 0 {
		// 如果是timeout了，后来任务执行完成之后，结果又上来了，stdout和stderr最好还是存库，让用户看到
		err = DB.Table(tbl(id)).Where("id=? and hostname=? and status=?", id, hostname, "timeout").Count(&count).Error
		if err != nil {
			return err
		}

		if count == 1 {
			return DB.Table(tbl(id)).Where("id=? and hostname=?", id, hostname).Updates(map[string]interface{}{"status": status, "stdout": stdout, "stderr": stderr}).Error
		}

		return nil
	}

	tx := DB.Begin()
	if err := tx.Error; err != nil {
		return err
	}

	if err := tx.Table(tbl(id)).Where("id=? and hostname=?", id, hostname).Updates(map[string]interface{}{"status": status, "stdout": stdout, "stderr": stderr}).Error; err != nil {
		tx.Rollback()
		return err
	}

	if err := tx.Exec("DELETE FROM host_doing WHERE id=? and hostname=?", id, hostname).Error; err != nil {
		tx.Rollback()
		return err
	}

	ingCount, err := IngStatusCount(id)
	if err != nil {
		return err
	}

	if ingCount > 0 {
		return fmt.Errorf("has nostop status")
	}

	// 这里处理下action
	if err := tx.Exec("DELETE FROM task_action WHERE id = ?", id).Error; err != nil {
		tx.Rollback()
		return err
	}

	return tx.Commit().Error
}

// WaitingCount 等待被调度执行的机器数量
func WaitingCount(id int64) (int, error) {
	var count int
	err := DB.Table(tbl(id)).Where("id = ? and status = ?", id, "waiting").Count(&count).Error
	return count, err
}

// WaitingHosts 等待被调度执行的机器列表
func WaitingHosts(id int64, limit ...int) ([]TaskHost, error) {
	var hosts []TaskHost
	orm := DB.Table(tbl(id)).Where("id = ? and status = 'waiting'", id).Order("ii")
	if len(limit) > 0 {
		orm = orm.Limit(limit[0])
	}
	err := orm.Find(&hosts).Error
	return hosts, err
}

func UnexpectedCount(id int64) (int, error) {
	var count int
	err := DB.Table(tbl(id)).Where("id = ? and status in ('failed', 'timeout', 'killfailed')", id).Count(&count).Error
	return count, err
}

func IngStatusCount(id int64) (int, error) {
	var count int
	params := []string{"waiting", "running", "killing"}
	spStatus := config.G.SpNoFinStatus
	for _, v := range spStatus {
		params = append(params, v)
	}

	err := DB.Table(tbl(id)).Where("id = ? and status in (?)", id, params).Count(&count).Error
	return count, err
}

// timeout-begin
// 判断最后一个任务是否为timeout状态
func IsLastTaskTimeout(id int64) (bool, error) {
	var taskHost TaskHost
	err := DB.Table(tbl(id)).Where("id = ?", id).Order("ii DESC").First(&taskHost).Error
	if err != nil {
		return false, err
	}

	if taskHost.Status == "timeout" {
		return true, nil
	}

	return false, nil
}

// timeout-end

