package timer

import (
	"log"
	"time"

	"zeus/scheduler/backend"
	"zeus/scheduler/config"
)

// Schedule 每秒探测一次，看有哪些task还未分配，抢之，抢了之后就调度task执行
// 有些任务Report之后，部分机器执行结束，需要继续重新调度，我就去找哪些任务是属于我的，我来搞起
func Schedule() {
	for {
		scheduleOrphan()
		scheduleMine()
		time.Sleep(time.Second)
	}
}

func scheduleMine() {
	ids, err := backend.MyTask()
	log.Printf("[D] scheduleMine ids:%v", ids)
	if err != nil {
		log.Println("[E] MyTask fail:", err)
		return
	}

	count := len(ids)
	for i := 0; i < count; i++ {
		log.Printf("[D] scheduleMine id:%d", i)
		backend.ScheduleTask(ids[i])
	}
}

func scheduleOrphan() {
	ids, err := backend.OrphanTaskIds()
	if err != nil {
		log.Println("[E] OrphanTaskIds fail", err)
		return
	}

	count := len(ids)
	if count == 0 {
		return
	}

	if config.G.Debug {
		log.Println("[D] orphan task ids:", ids)
	}

	endpoint, err := config.Endpoint()
	if err != nil {
		log.Println("[E] get endpoint fail:", err)
		return
	}

	for i := 0; i < count; i++ {
		// 新建的任务，task_action是pause，不用管
		action, err := backend.GetTaskAction(ids[i])
		if err != nil {
			log.Println("[E] GetTaskAction fail:", err)
			continue
		}

		if action.Action == "pause" {
			continue
		}

		mine, err := backend.TakeOverTask(ids[i], "", endpoint)
		if err != nil {
			log.Println("[E] TakeOverTask fail:", err)
			continue
		}

		if !mine {
			continue
		}

		if config.G.Debug {
			log.Printf("[D] %d is mine", ids[i])
		}

		err = backend.ScheduleTask(ids[i])
		if err != nil {
			log.Println("[E] ScheduleTask fail:", err)
			continue
		}
	}
}
