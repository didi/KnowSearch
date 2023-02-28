package timer

import (
	"errors"
	"log"
	"math/rand"
	"net"
	"os"
	"time"

	"zeus/agent/client"
	"zeus/agent/config"
	"zeus/common/model"
)

func Heartbeat() {
	// 随机sleep一段时间，防止太多agent同时请求server端，对server端造成压力
	rand.Seed(time.Now().UnixNano() + int64(os.Getpid()+os.Getppid()))
	d := rand.Intn(10000)
	log.Printf("[I] sleep %dms then heartbeat\n", d)
	time.Sleep(time.Duration(d) * time.Millisecond)

	for {
		heartbeat()
		time.Sleep(time.Duration(config.G.Interval) * time.Second)
	}
}

func heartbeat() {
	hostname := config.G.Hostname
	if hostname == "" {
		out, err := os.Hostname()
		if err != nil {
			log.Println("[E] os.Hostname fail:", err)
			return
		}
		hostname = out
	}

	base := config.GetBase()
	if base["ip"] == "" {
		base["ip"], _ = getClientIp()
	}

	req := model.ReportRequest{
		Hostname:    hostname,
		Baseinfo:    base,
		ReportTasks: Locals.ReportTasks(),
	}

	var resp model.ReportResponse
	err := client.GetExecClient().Call("Exec.Report", req, &resp, 0)
	if err != nil {
		log.Println("[E] rpc call Exec.Report fail:", err)
		client.CloseExecClient()
		return
	}

	if resp.Message != "" {
		log.Println("[E] error from exec:", resp.Message)
		return
	}

	assigned := make(map[int64]struct{})

	if resp.AssignTasks != nil {
		count := len(resp.AssignTasks)
		for i := 0; i < count; i++ {
			at := resp.AssignTasks[i]
			assigned[at.ID] = struct{}{}
			Locals.AssignTask(at)
		}
	}

	Locals.Clean(assigned)
}

func getClientIp() (string, error) {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return "", err
	}
	for _, address := range addrs {
		// 检查ip地址判断是否回环地址
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				return ipnet.IP.String(), nil
			}
		}
	}

	return "", errors.New("Can not find the client ip address!")
}
