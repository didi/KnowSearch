package timer

import (
	"log"

	"zeus/common/model"
)

type LocalTasksT struct {
	M map[int64]*Task
}

// Locals 保存当前运行的任务
var Locals = &LocalTasksT{M: make(map[int64]*Task)}

// ReportTasks 收集各个任务的状态，汇报
func (lt *LocalTasksT) ReportTasks() []model.ReportTask {
	ret := make([]model.ReportTask, 0, len(lt.M))
	for id, t := range lt.M {
		rt := model.ReportTask{ID: id, TS: t.TS}

		rt.Status = t.GetStatus()
		if rt.Status == "running" || rt.Status == "killing" {
			// 中间状态，无需汇报
			continue
		}

		rt.Stdout = t.GetStdout()
		rt.Stderr = t.GetStderr()

		stdoutLen := len(rt.Stdout)
		stderrLen := len(rt.Stderr)

		// 输出太长的话，截断，要不然把数据库撑爆了
		if stdoutLen > 65535 {
			start := stdoutLen - 65535
			rt.Stdout = rt.Stdout[start:]
		}

		if stderrLen > 65535 {
			start := stderrLen - 65535
			rt.Stderr = rt.Stderr[start:]
		}

		ret = append(ret, rt)
	}

	return ret
}

// GetTask 根据任务ID获取任务
func (lt *LocalTasksT) GetTask(id int64) (*Task, bool) {
	t, found := lt.M[id]
	return t, found
}

// SetTask 将任务放到本地内存
func (lt *LocalTasksT) SetTask(t *Task) {
	lt.M[t.ID] = t
}

// AssignTask 处理下发的单条任务
func (lt *LocalTasksT) AssignTask(at model.AssignTask) {
	local, found := lt.GetTask(at.ID)
	if found {
		if local.TS == at.TS && local.Action == at.Action {
			// 重复下发，忽略
			return
		}

		local.TS = at.TS
		local.Action = at.Action
	} else {
		if at.Action == "kill" {
			// 本地根本就没有对应进程，无法执行kill
			return
		}
		local = &Task{
			ID:     at.ID,
			TS:     at.TS,
			Action: at.Action,
		}
		lt.SetTask(local)

		// 实际这个任务已经做过了，莫非是上层丢了数据，又分配下来了
		// 读取之前的结果
		if local.doneBefore() {
			local.loadBeforeResult()
			return
		}
	}

	if local.Action == "kill" {
		local.SetStatus("killing")
		local.kill()
	} else if local.Action == "start" {
		local.SetStatus("running")
		local.start()
	} else {
		log.Printf("[W] unknown action: %s of task %d\n", at.Action, at.ID)
	}
}

// Clean 本地内存的任务如果没有出现在下发的任务列表中，说明已经不用关心了
func (lt *LocalTasksT) Clean(assigned map[int64]struct{}) {
	del := make(map[int64]struct{})

	for id := range lt.M {
		if _, found := assigned[id]; !found {
			del[id] = struct{}{}
		}
	}

	for id := range del {

		// 远端已经不关注这个任务了，但是本地来看，任务还是running的
		// 可能是远端认为超时了，此时本地不能删除，仍然要继续上报
		if lt.M[id].GetStatus() == "running" {
			continue
		}

		lt.M[id].ResetBuff()
		cmd := lt.M[id].Cmd
		delete(lt.M, id)
		// 如果无法释放，也没招了
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Release()
		}
	}
}
