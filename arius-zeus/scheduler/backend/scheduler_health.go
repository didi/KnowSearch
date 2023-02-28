package backend

// SchedulerHeartbeat 更新本scheduler的心跳时间戳
func SchedulerHeartbeat(endpoint string) error {
	var err error
	var count int
	if err = DB.Table("scheduler_health").LogMode(false).Where("scheduler = ?", endpoint).Count(&count).Error; err != nil {
		return err
	}

	if count == 0 {
		err = DB.Debug().LogMode(false).Exec("INSERT INTO scheduler_health(scheduler, ts) VALUES(?, now())", endpoint).Error
	} else {
		err = DB.Debug().LogMode(false).Exec("UPDATE scheduler_health SET ts = now() WHERE scheduler = ?", endpoint).Error
	}

	return err
}

// DeadSchedulers 获取已经挂掉的scheduler列表，10s都没心跳了，认为该scheduler挂了
func DeadSchedulers() ([]string, error) {
	var schedulers []string
	err := DB.Table("scheduler_health").LogMode(false).Where("ts < DATE_SUB(now(),INTERVAL 10 SECOND)").Pluck("scheduler", &schedulers).Error
	return schedulers, err
}

// DelDeadScheduler 删除已经死翘翘的scheduler，如果死了但是不删除，
// 其他scheduler就会一直要执行“查询该scheduler负责的任务并接管”这个逻辑
func DelDeadScheduler(scheduler string) error {
	return DB.Exec("DELETE FROM scheduler_health WHERE scheduler = ?", scheduler).Error
}
