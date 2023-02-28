package backend

type HostDoing struct {
	ID       int64  `gorm:"column:id"`
	Hostname string `gorm:"hostname"`
	TS       int    `gorm:"column:ts"`
	Action   string `gorm:"action"`
}

func DoingHosts(id int64) ([]HostDoing, error) {
	var hosts []HostDoing
	err := DB.Table("host_doing").Where("id = ?", id).Find(&hosts).Error
	return hosts, err
}

func DoingHostCount(id int64) (int, error) {
	var count int
	err := DB.Table("host_doing").Where("id = ?", id).Count(&count).Error
	return count, err
}

func DoingHostsByHostname(hostname string) ([]HostDoing, error) {
	var hosts []HostDoing
	err := DB.Table("host_doing").Where("hostname=?", hostname).Find(&hosts).Error
	return hosts, err
}
