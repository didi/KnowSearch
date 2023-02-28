package backend

import (
	"log"
	"time"
)

type EdgeAgent struct {
	Ident string `gorm:"ident"`
	Ip    string `gorm:"ip"`
	TS    int    `gorm:"column:ts"`
}

func EdgeAgentInfo(ident string, base map[string]string) error {
	var err error
	var count int
	if err = DB.Table("edge_agent_info").LogMode(false).Where("ident = ?", ident).Count(&count).Error; err != nil {
		return err
	}

	//当前只入库IP,其他信息若需入库，参考IP
	now := time.Now().Unix()
	ip := base["ip"]
	if ip == "" {
		log.Printf("%s 未获取到IP信息", ident)
		if count == 0 {
			err = DB.Debug().LogMode(false).Exec("INSERT INTO edge_agent_info(ident, ts) VALUES(?, ?)", ident, now).Error
		} else {
			err = DB.Debug().LogMode(false).Exec("UPDATE edge_agent_info SET ts = ? WHERE ident = ?", now, ident).Error
		}

		return err
	}

	if count == 0 {
		err = DB.Debug().LogMode(false).Exec("INSERT INTO edge_agent_info(ident, ip, ts) VALUES(?, ?, ?)", ident, ip, now).Error
	} else {
		err = DB.Debug().LogMode(false).Exec("UPDATE edge_agent_info SET ip = ?, ts = ? WHERE ident = ?", ip, now, ident).Error
	}

	return err
}
