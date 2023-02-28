package model

import (
	"strings"
)

func EdgeAgentCount(query string) (int64, error) {
	orm := DB.Model(&EdgeAgentInfo{})
	if query != "" {
		// q1 q2 -q3
		arr := strings.Fields(query)
		for i := 0; i < len(arr); i++ {
			if arr[i] == "" {
				continue
			}
			if strings.HasPrefix(arr[i], "-") {
				q := "%" + arr[i][1:] + "%"
				orm = orm.Where("ident not like ? and ip not like ?", q, q)
			} else {
				q := "%" + arr[i] + "%"
				orm = orm.Where("ident like ? or ip like ?", q, q)
			}
		}
	}

	var total int64
	err := orm.Count(&total).Error
	return total, err
}

func EdgeAgentList(query string, limit, offset int) ([]EdgeAgentInfo, error) {
	ret := []EdgeAgentInfo{}
	orm := DB.Limit(limit).Offset(offset).Order("ident desc")
	if query != "" {
		// q1 q2 -q3
		arr := strings.Fields(query)
		for i := 0; i < len(arr); i++ {
			if arr[i] == "" {
				continue
			}
			if strings.HasPrefix(arr[i], "-") {
				q := "%" + arr[i][1:] + "%"
				orm = orm.Where("ident not like ? and ip not like ?", q, q)
			} else {
				q := "%" + arr[i] + "%"
				orm = orm.Where("ident like ? or ip like ?", q, q)
			}
		}
	}

	err := orm.Find(&ret).Error
	return ret, err
}
