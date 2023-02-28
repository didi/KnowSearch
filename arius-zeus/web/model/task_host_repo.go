package model

var TaskHostRepo = new(TaskHost)

func (*TaskHost) FindStdout(id int64) ([]TaskHost, error) {
	var ret []TaskHost
	err := DB.Table(tbl(id)).Where("id=?", id).Order("ii").Find(&ret).Error
	return ret, err
}

func (*TaskHost) FindStatus(id int64) ([]TaskHost, error) {
	var ret []TaskHost
	err := DB.Table(tbl(id)).Select("id, hostname, status").Where("id=?", id).Order("ii").Find(&ret).Error
	return ret, err
}

func (*TaskHost) FindByStatus(id int64, status string) ([]TaskHost, error) {
	var ret []TaskHost
	err := DB.Table(tbl(id)).Select("id, hostname, status").Where("id=? and status=?", id, status).Order("ii").Find(&ret).Error
	return ret, err
}

func (*TaskHost) Get(id int64, hostname string) (*TaskHost, error) {
	var ret []*TaskHost
	err := DB.Table(tbl(id)).Where("id=? and hostname=?", id, hostname).Find(&ret).Error
	if err != nil {
		return nil, err
	}

	if len(ret) == 0 {
		return nil, nil
	}

	return ret[0], nil
}
