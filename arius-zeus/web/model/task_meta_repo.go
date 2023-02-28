package model

import (
	"fmt"
	"strings"
	"time"

	"github.com/toolkits/cache"

	"zeus/web/utils"
)

func TaskMetaGet(id int64) (*TaskMeta, error) {
	var obj TaskMeta
	if err := cache.Get(taskMetaCacheKey(id), &obj); err == nil {
		return &obj, nil
	}

	var objs []TaskMeta
	err := DB.Where("id=?", id).Find(&objs).Error
	if err != nil {
		return nil, err
	}

	if len(objs) == 0 {
		return nil, nil
	}

	cache.Set(taskMetaCacheKey(id), objs[0], time.Hour)
	return &objs[0], nil
}

func TaskMetaCount(creator, query string) (int64, error) {
	orm := DB.Model(&TaskMeta{})
	if creator != "" {
		orm = orm.Where("creator=?", creator)
	}
	if query != "" {
		// q1 q2 -q3
		arr := strings.Fields(query)
		for i := 0; i < len(arr); i++ {
			if arr[i] == "" {
				continue
			}
			if strings.HasPrefix(arr[i], "-") {
				q := "%" + arr[i][1:] + "%"
				orm = orm.Where("keywords not like ?", q)
			} else {
				q := "%" + arr[i] + "%"
				orm = orm.Where("keywords like ?", q)
			}
		}
	}

	var total int64
	err := orm.Count(&total).Error
	return total, err
}

func TaskMetaList(creator, query string, limit, offset int) ([]TaskMeta, error) {
	ret := []TaskMeta{}
	orm := DB.Limit(limit).Offset(offset).Order("id desc")
	if creator != "" {
		orm = orm.Where("creator=?", creator)
	}
	if query != "" {
		// q1 q2 -q3
		arr := strings.Fields(query)
		for i := 0; i < len(arr); i++ {
			if arr[i] == "" {
				continue
			}
			if strings.HasPrefix(arr[i], "-") {
				q := "%" + arr[i][1:] + "%"
				orm = orm.Where("keywords not like ?", q)
			} else {
				q := "%" + arr[i] + "%"
				orm = orm.Where("keywords like ?", q)
			}
		}
	}

	err := orm.Find(&ret).Error
	return ret, err
}

func TaskMetaInsert(meta *TaskMeta, hostnames string, action string) error {
	if err := meta.CleanFields(); err != nil {
		return err
	}

	hosts := utils.ParseLines(hostnames)

	// 如果机器有多台并没有设置暂停点，强制第一台暂停
	// if meta.Pause == "" && len(hosts) > 1 {
	// 	meta.Pause = hosts[0]
	// }

	// 为了便于查看，将任务的第一台机器强制放置到keywords当中
	// 但如果这是fork的任务，keywords当中肯定已经带有FH字样了，删除先
	idx := strings.Index(meta.Keywords, " FH: ")
	if idx > 0 {
		meta.Keywords = meta.Keywords[:idx]
	}
	meta.Keywords = meta.Keywords + " FH: " + hosts[0]

	tx := DB.Begin()
	if tx.Error != nil {
		return fmt.Errorf("cannot begin transaction")
	}

	if err := tx.Create(meta).Error; err != nil {
		tx.Rollback()
		return err
	}

	id := meta.ID

	if err := tx.Create(&TaskScheduler{ID: id}).Error; err != nil {
		tx.Rollback()
		return err
	}

	if err := tx.Create(&TaskAction{ID: id, Action: action, TS: meta.Created}).Error; err != nil {
		tx.Rollback()
		return err
	}

	if meta.Pause != "" {
		arr := strings.Split(meta.Pause, ",")
		for _, p := range arr {
			sql := fmt.Sprintf("INSERT INTO task_pause(id, hostname, has) VALUES(%d, '%s', 0)", id, p)
			if err := tx.Exec(sql).Error; err != nil {
				tx.Rollback()
				return err
			}
		}
	}

	for _, host := range hosts {
		if err := tx.Exec(fmt.Sprintf("INSERT INTO %s(id, hostname, status) VALUES(%d, '%s', 'waiting')", tbl(id), id, host)).Error; err != nil {
			tx.Rollback()
			return err
		}
	}

	if tx.Commit().Error != nil {
		return fmt.Errorf("cannot commit transaction")
	}

	return nil
}
