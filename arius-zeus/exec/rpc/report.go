package rpc

import (
	"time"

	"zeus/common/model"
	"zeus/exec/backend"
)

func (*Exec) Report(req model.ReportRequest, resp *model.ReportResponse) error {
	return backend.SchedulerClients.Call("Scheduler.Report", req, resp, time.Second*time.Duration(10))
}
