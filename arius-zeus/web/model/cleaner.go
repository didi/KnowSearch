package model

import (
	"log"
	"time"
)

// StartCleaner 这种任务一般是用户忘记了，直接cancel掉
func StartCleaner() {
	go startCleaner()
}

func startCleaner() {
	d := time.Duration(24) * time.Hour
	for {
		time.Sleep(d)
		cleanLongTask()
	}
}

func cleanLongTask() {
	ids, err := LongTaskIDs()
	if err != nil {
		log.Println("[E] LongTaskIDs:", err)
		return
	}

	count := len(ids)
	if count == 0 {
		return
	}

	for i := 0; i < count; i++ {
		TaskActionRepo.Update(ids[i], "cancel")
	}
}
