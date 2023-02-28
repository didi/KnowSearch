package backend

import (
	"fmt"
	"log"
	"strings"
	"time"

	"zeus/scheduler/config"
)

func ScheduleTask(id int64) error {
	if config.G.Debug {
		log.Printf("[D] %d scheduling...", id)
	}

	count, err := WaitingCount(id)
	log.Printf("[D] %d ScheduleTask waiting count=%d ", id, count)
	if err != nil {
		log.Printf("[E] WaitingCount fail, id:%d\n", id)
		return err
	}

	if count == 0 {
		// 没有waiting状态了，则无需调度了
		// 但任务不能立马标记为结束，可能还有running没完事的
		MarkDoneTaskIfNeed(id)
		return nil
	}

	action, err := GetTaskAction(id)
	if err != nil {
		log.Println("[E] GetTaskAction fail:", err)
		return err
	}

	if action == nil {
		log.Printf("[W] WaitingCount(%d) = %d > 0, but action is gone, maybe task is scheduled by other scheduler and task is done already\n", id, count)
		return nil
	}

	// cancel和kill不需要由scheduler调度，web端直接就搞了，这俩动作是要快速执行的
	switch action.Action {
	case "start":
		if err = StartTask(id); err != nil {
			log.Println("[E] StartTask fail:", err)
			return err
		}
	case "pause":
		if err = PauseTask(id); err != nil {
			log.Println("[E] PauseTask fail:", err)
			return err
		}
	case "cancel":
		if err = CancelTask(id); err != nil {
			log.Println("[E] CancelTask fail:", err)
			return err
		}
	case "kill":
		if err = KillTask(id); err != nil {
			log.Println("[E] KillTask fail:", err)
			return err
		}
	default:
		log.Printf("[E] unknown action: %s of task %d", action.Action, id)
	}

	return nil
}

func MarkDoneTaskIfNeed(id int64) {
	task, err := GetTaskMeta(id)
	if err != nil {
		log.Println("[E] GetTaskMeta fail:", err)
		return
	}

	unExpCount, err := UnexpectedCount(id)
	if err != nil {
		log.Println("[E] UnexpectedCount fail:", err)
		return
	}

	if task == nil {
		log.Printf("[W] No taskmeta found, id: %d \n", id)
		return
	}

	if unExpCount > task.Tolerance {
		UpdateAction(id, "pause")
		return
	}

	ingCount, err := IngStatusCount(id)
	if err != nil {
		log.Println("[E] IngStatusCount fail:", err)
		return
	}

	if ingCount > 0 {
		return
	}

	// // timeout-begin
	// // 判断如果是最后一个子任务状态为timeout，就先不处理，直接返回
	// isLastTimeout, err := IsLastTaskTimeout(id)
	// if err != nil {
	// 	log.Println("[E] IsLastTaskTimeout fail:", err)
	// 	return
	// }
	// if isLastTimeout {
	// 	log.Printf("[D] Last task timeout, id: %d \n", id)
	// 	return
	// }
	// // timeout-end

	tx := DB.Begin()
	if err := tx.Exec("DELETE FROM task_scheduler WHERE id = ?", id).Error; err != nil {
		log.Println("[E]", err)
		tx.Rollback()
		return
	}
	if err := tx.Exec("DELETE FROM task_action WHERE id = ?", id).Error; err != nil {
		log.Println("[E]", err)
		tx.Rollback()
		return
	}
	if err := tx.Exec("DELETE FROM task_pause WHERE id = ?", id).Error; err != nil {
		log.Println("[E]", err)
		tx.Rollback()
		return
	}
	tx.Commit()
}

func StartTask(id int64) error {
	task, err := GetTaskMeta(id)
	if err != nil {
		log.Println("[E] GetTaskMeta fail:", err)
		return err
	}

	count, err := UnexpectedCount(id)
	if err != nil {
		log.Println("[E] UnexpectedCount fail:", err)
		return err
	}

	if task == nil {
		log.Printf("[W] No taskmeta found, id: %d \n", id)
		return fmt.Errorf("no taskmeta found, id: %d", id)
	}

	if count > task.Tolerance {
		UpdateAction(id, "pause")
		return nil
	}

	waitting_hosts, err := WaitingHosts(id)
	if err != nil {
		log.Println("[E] WaitingHosts fail:", err)
		return err
	}

	if len(waitting_hosts) == 0 {
		return nil
	}

	// task.Batch - 已经在跑的数目 = 还要调度执行的数目
	doingCount, err := DoingHostCount(id)
	if err != nil {
		log.Println("[E] DoingHostCount fail:", err)
		return err
	}

	need := task.Batch - doingCount

	if task.Batch == 0 {
		need = len(waitting_hosts)
	}

	if need <= 0 {
		return nil
	}

	if need > len(waitting_hosts) {
		need = len(waitting_hosts)
	}

	// pause=10.1.1.1,10.2.2.2
	// 调度到任一个暂停点都需要暂停并且不向下运行
	var pause_hostnames []string
	if task.Pause != "" {
		// 处理暂停点
		pause_hostnames = strings.Split(task.Pause, ",")
	}

	for i := 0; i < need; i++ {
		for _, pause_hostname := range pause_hostnames {
			if pause_hostname == "" {
				continue
			}
			if pause_hostname == waitting_hosts[i].Hostname {
				//到暂停点了，需要暂停，但是暂停点的机器本身还是要跑的
				err = RunWaitingHosts(id, i+1)
				if err != nil {
					log.Println("[E] RunWaitingHosts fail:", err)
					return err
				}

				err = UpdateAction(id, "pause")
				if err != nil {
					log.Println("[E] UpdateAction fail:", err)
					return err
				}

				return nil
			}
		}
	}

	return RunWaitingHosts(id, need)
}

// RunWaitingHosts 执行某任务的所有待执行机器
func RunWaitingHosts(id int64, limit ...int) error {
	hosts, err := WaitingHosts(id, limit...)
	if err != nil {
		return err
	}

	count := len(hosts)
	if count == 0 {
		return nil
	}

	now := time.Now().Unix()
	for i := 0; i < count; i++ {
		tx := DB.Begin()
		if err := tx.Error; err != nil {
			return err
		}

		if err := tx.Table(tbl(id)).Where("id=? and hostname=?", id, hosts[i].Hostname).Update("status", "running").Error; err != nil {
			tx.Rollback()
			return err
		}

		if err := tx.Exec("INSERT INTO host_doing(id, hostname, ts, action) VALUES(?, ?, ?, 'start')", id, hosts[i].Hostname, now).Error; err != nil {
			tx.Rollback()
			return err
		}

		if err := tx.Commit().Error; err != nil {
			return err
		}
	}

	return nil
}

func PauseTask(id int64) error {
	// 既然暂停了，就不要继续调度了
	return nil
}

func CancelTask(id int64) error {
	// cancel的逻辑挪到web上去
	return nil
}

func KillTask(id int64) error {
	// kill的逻辑挪到web上去
	return nil
}

// CheckTimeout 检查各个机器执行该任务执行了多久了，是否超时
func CheckTimeout(id int64) error {
	t, err := GetTaskMeta(id)
	if err != nil {
		return err
	}

	// 找到当前正在运行的那些机器，看看哪些执行超时了
	hosts, err := DoingHosts(id)
	if err != nil {
		return err
	}

	count := len(hosts)
	if count == 0 {
		return nil
	}

	hasTimeout := false
	// 3这个magic number：
	// 任务下发需要时间，3s，任务肯定下发成功了吧，得减去任务下发的时间才是单机执行的时间
	rv := t.Timeout + 3

	now := int(time.Now().Unix())
	for i := 0; i < count; i++ {
		if now-hosts[i].TS > rv {
			MarkDoneStatus(hosts[i].ID, hosts[i].TS, hosts[i].Hostname, "timeout", "", "")
			hasTimeout = true
		}
	}

	// //timeout-begin
	// // 临时解决，如果最后一个子任务的状态为timeout，那就将task_action处理成pause状态，可以继续调度
	// isLastTimeout, err := IsLastTaskTimeout(id)
	// if err == nil && isLastTimeout {
	// 	UpdateAction(id, "pause")
	// }
	// //timeout-end

	statusOk := true
	for _, v := range config.G.SpNoFinStatus {
		if v == "timeout" {
			statusOk = false
		}
	}

	// 只要某个机器的执行状态发生了变化，就需要重新调度
	if hasTimeout && statusOk {
		ScheduleTask(id)
	}

	return nil
}
