package backend

import (
	"time"

	"zeus/common/net/gobrpc"
	"zeus/exec/config"
)

var SchedulerClients *gobrpc.Clients

func InitClients() {
	SchedulerClients = gobrpc.NewClients(config.G.Schedulers, time.Duration(10)*time.Second)
}
