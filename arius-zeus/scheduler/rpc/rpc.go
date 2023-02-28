package rpc

import (
	"log"
	"net"
	"net/rpc"

	"zeus/scheduler/config"
)

type Scheduler int

func Start() {
	addr := config.G.Listen
	tcpAddr, err := net.ResolveTCPAddr("tcp", addr)
	if err != nil {
		log.Fatalf("net.ResolveTCPAddr fail: %s", err)
	}

	listener, err := net.ListenTCP("tcp", tcpAddr)
	if err != nil {
		log.Fatalf("listen %s fail: %s", addr, err)
	} else {
		log.Println("[I] rpc listening on", addr)
	}

	rpc.Register(new(Scheduler))

	for {
		conn, err := listener.Accept()
		if err != nil {
			log.Println("[W] listener.Accept occur error", err)
			continue
		}
		go rpc.ServeConn(conn)
	}
}
