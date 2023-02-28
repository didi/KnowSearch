package client

import (
	"log"
	"net"
	"net/rpc"
	"time"

	"zeus/agent/config"
	"zeus/common/net/gobrpc"
)

var execClient *gobrpc.RPCClient

func getExecClient() *gobrpc.RPCClient {
	// 之前已经建立了长连接，直接返回client
	if execClient != nil {
		return execClient
	}

	// 初次建立长连接，找一个访问速度比较快的exec
	var (
		address  string      = ""
		client   *rpc.Client = nil
		duration int64       = 999999999999
	)

	// 除了最快的那个，其他的client都要close
	acm := make(map[string]*rpc.Client)

	l := len(config.G.Servers)
	for i := 0; i < l; i++ {
		addr := config.G.Servers[i]
		begin := time.Now()
		conn, err := net.DialTimeout("tcp", addr, time.Second*5)
		if err != nil {
			log.Println("[W] dial", addr, "fail", err)
			continue
		}

		c := rpc.NewClient(conn)
		acm[addr] = c

		var out string
		err = c.Call("Exec.Ping", "", &out)
		if err != nil {
			log.Println("[W] ping", addr, "fail", err)
			continue
		}
		use := time.Since(begin).Nanoseconds()

		if use < duration {
			address = addr
			client = c
			duration = use
		}
	}

	if address == "" {
		log.Println("[E] no exec found")
		return nil
	}

	log.Printf("[I] choose exec: %s, duration: %dms\n", address, duration/1000000)

	for addr, c := range acm {
		if addr == address {
			continue
		}
		c.Close()
	}

	execClient = gobrpc.NewRPCClient(address, client, 10*time.Second)
	return execClient
}

func GetExecClient() *gobrpc.RPCClient {
	for {
		c := getExecClient()
		if c != nil {
			return c
		}

		time.Sleep(time.Second * 10)
	}
}

func CloseExecClient() {
	if execClient != nil {
		execClient.Close()
		execClient = nil
	}
}
