package timer

import (
	"log"
	"time"

	"zeus/scheduler/backend"
	"zeus/scheduler/config"
)

// Heartbeat Scheduler要不断心跳表明自己还活着，同时查看有哪些scheduler挂了
// 接管挂了的scheduler负责的任务
func Heartbeat() {
	for {
		heartbeat()
		time.Sleep(time.Second)
	}
}

func heartbeat() {
	endpoint, err := config.Endpoint()
	if err != nil {
		log.Println("[E] get endpoint fail:", err)
		return
	}

	err = backend.SchedulerHeartbeat(endpoint)
	if err != nil {
		log.Println("[E] SchedulerHeartbeat fail:", err)
		return
	}

	ss, err := backend.DeadSchedulers()
	if err != nil {
		log.Println("[E] DeadSchedulers fail:", err)
		return
	}

	count := len(ss)
	if count == 0 {
		return
	}

	for i := 0; i < count; i++ {
		ids, err := backend.TasksOfScheduler(ss[i])
		if err != nil {
			log.Println("[E] TasksOfScheduler fail:", err)
			return
		}

		if len(ids) == 0 {
			// 这个挂掉的scheduler没有负责任何task，留你何用
			backend.DelDeadScheduler(ss[i])
		}

		takeOverTasks(endpoint, ss[i], ids)
	}
}

func takeOverTasks(alive, dead string, ids []int64) {
	count := len(ids)
	for i := 0; i < count; i++ {
		success, err := backend.TakeOverTask(ids[i], dead, alive)
		if err != nil {
			log.Println("[E] TakeOverTask fail:", err)
			return
		}

		if !success {
			continue
		}

		// 我接管了这个任务，就直接去调度即可，不需要再写入task_scheduling表，多此一举
		err = backend.ScheduleTask(ids[i])
		if err != nil {
			log.Println("[E] ScheduleTask fail:", err)
		}
	}
}
