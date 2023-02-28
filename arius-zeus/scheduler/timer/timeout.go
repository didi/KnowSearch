package timer

import (
	"log"
	"time"

	"zeus/scheduler/backend"
)

// CheckTimeout 查看我负责了哪些task
// 每个task查看各个机器的运行情况，如果有超时，就标识一下，继续调度
func CheckTimeout() {
	for {
		checkTimeout()
		time.Sleep(5 * time.Second)
	}
}

func checkTimeout() {
	ids, err := backend.MyTask()
	if err != nil {
		log.Println("[E] MyTask fail:", err)
		return
	}

	count := len(ids)
	for i := 0; i < count; i++ {
		err = backend.CheckTimeout(ids[i])
		if err != nil {
			log.Println("[E] CheckTimeout fail:", err)
			return
		}
	}
}
