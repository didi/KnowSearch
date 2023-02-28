package rpc

import (
	"log"

	"zeus/common/model"
	"zeus/scheduler/backend"
)

func (*Scheduler) Report(req model.ReportRequest, resp *model.ReportResponse) error {
	if req.ReportTasks != nil && len(req.ReportTasks) > 0 {
		err := handleDoneTask(req)
		if err != nil {
			resp.Message = err.Error()
			return nil
		}
	}

	err := backend.EdgeAgentInfo(req.Hostname, req.Baseinfo)
	if err != nil {
		return err
	}

	hosts, err := backend.DoingHostsByHostname(req.Hostname)
	if err != nil {
		resp.Message = err.Error()
		return nil
	}

	l := len(hosts)
	tasks := make([]model.AssignTask, l)
	for i := 0; i < l; i++ {
		tasks[i].ID = hosts[i].ID
		tasks[i].TS = hosts[i].TS
		tasks[i].Action = hosts[i].Action
	}

	resp.AssignTasks = tasks
	return nil
}

func handleDoneTask(req model.ReportRequest) error {
	count := len(req.ReportTasks)
	for i := 0; i < count; i++ {
		t := req.ReportTasks[i]
		// timeout-begin
		//if t.Status == "timeout" {
		//	log.Printf("[E] MarkDoneStatus break, id:%d, hostname:%s, ts:%d, status:%s\n", t.ID, req.Hostname, t.TS, t.Status)
		//	return nil
		//}
		// timeout-end
		err := backend.MarkDoneStatus(t.ID, t.TS, req.Hostname, t.Status, t.Stdout, t.Stderr)
		if err != nil {
			log.Printf("[E] MarkDoneStatus fail, id:%d, hostname:%s, ts:%d, status:%s\n", t.ID, req.Hostname, t.TS, t.Status)
			return err
		}
	}

	return nil
}
