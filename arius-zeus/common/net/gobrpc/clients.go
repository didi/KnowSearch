package gobrpc

import (
	"fmt"
	"log"
	"math/rand"
	"net/rpc"
	"strings"
	"sync"
	"time"
)

type Clients struct {
	sync.RWMutex
	clients     map[string]*RPCClient
	addresses   []string
	callTimeout time.Duration
}

func NewClients(addresses []string, callTimeout time.Duration) *Clients {
	cs := &Clients{}
	cs.addresses = addresses
	cs.clients = make(map[string]*RPCClient)

	count := len(addresses)
	if count == 0 {
		log.Fatalln("[F] addresses are empty")
	}

	for i := 0; i < count; i++ {
		endpoint := addresses[i]
		client, err := NewRawClient("tcp", endpoint, time.Duration(5)*time.Second)
		if err != nil {
			log.Fatalln("[F] cannot connect to", endpoint)
		}
		cs.clients[endpoint] = NewRPCClient(endpoint, client, callTimeout)
	}

	return cs
}

// 这里的做法很简单，addresses列表一旦初始化设置好，就不变了，所以获取的时候也无需加锁
// 以后可以考虑做一个健康检查自动摘掉坏的实例，那个时候就要加锁了，复杂性增加，以后再说
func (cs *Clients) GetAddresses() []string {
	return cs.addresses
}

func (cs *Clients) SetClients(clients map[string]*RPCClient) {
	cs.Lock()
	cs.clients = clients
	cs.Unlock()
}

func (cs *Clients) PutClient(addr string, client *RPCClient) {
	cs.Lock()
	c, has := cs.clients[addr]
	if has && c != nil {
		c.Close()
	}

	cs.clients[addr] = client
	cs.Unlock()
}

func (cs *Clients) GetClient(addr string) (*RPCClient, bool) {
	cs.RLock()
	c, has := cs.clients[addr]
	cs.RUnlock()
	return c, has
}

func (cs *Clients) Call(method string, args, reply interface{}, callTimeout time.Duration) error {
	addrs := cs.GetAddresses()
	l := len(addrs)

	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	for _, i := range r.Perm(l) {
		addr := addrs[i]
		client, has := cs.GetClient(addr)
		if !has {
			log.Println("[W]", addr, "has no client")
			continue
		}

		if client.IsClose() {
			rawClient, err := NewRawClient("tcp", addr, time.Duration(5)*time.Second)
			if err != nil {
				log.Println("[W]", addr, "is dead")
				continue
			}

			client = NewRPCClient(addr, rawClient, cs.callTimeout)
			cs.PutClient(addr, client)

		}

		err := client.Call(method, args, reply, callTimeout)
		if err == nil {
			return nil
		}

		client.Close()
		if err == rpc.ErrShutdown || strings.Contains(err.Error(), "connection refused") {
			// 为了防止后端不是幂等的，只有后端是这两种情况的时候才敢continue，尝试调用其他的后端
			continue
		} else {
			return err
		}
	}

	return fmt.Errorf("[E] all backends are dead")
}
