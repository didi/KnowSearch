package backend

import (
	"fmt"
	"log"

	"github.com/jinzhu/gorm"

	"zeus/scheduler/config"
)

var DB *gorm.DB

func InitMySQL() {
	var err error
	DB, err = gorm.Open("mysql", config.G.MySQL.Addr)
	if err != nil {
		log.Fatalln("[F] fail to connect mysql", config.G.MySQL.Addr, err)
	}

	DB.LogMode(config.G.MySQL.ShowSQL)
	DB.DB().SetMaxIdleConns(config.G.MySQL.Idle)
	DB.DB().SetMaxOpenConns(config.G.MySQL.Max)
}

func tbl(id int64) string {
	return fmt.Sprintf("task_host_%d", id%config.G.Table)
}
