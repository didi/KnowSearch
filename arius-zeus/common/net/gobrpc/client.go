package gobrpc

import (
	"fmt"
	"log"
	"net"
	"net/rpc"
	"time"
)

type RPCClient struct {
	address     string
	rpcClient   *rpc.Client
	callTimeout time.Duration
}

func NewRawClient(network, address string, connTimeout time.Duration) (*rpc.Client, error) {
	conn, err := net.DialTimeout(network, address, connTimeout)
	if err != nil {
		return nil, err
	}
	return rpc.NewClient(conn), err
}

func NewRPCClient(address string, rpcClient *rpc.Client, callTimeout time.Duration) *RPCClient {
	return &RPCClient{
		address:     address,
		rpcClient:   rpcClient,
		callTimeout: callTimeout,
	}
}

func (c *RPCClient) Close() {
	if c.rpcClient != nil {
		c.rpcClient.Close()
		c.rpcClient = nil
	}
}

func (c *RPCClient) IsClose() bool {
	return c.rpcClient == nil
}

func (c *RPCClient) Call(method string, args interface{}, reply interface{}, callTimeout time.Duration) error {
	done := make(chan error, 1)

	go func() {
		if c.rpcClient == nil {
			// timeout, closed
			log.Println("[W] timeout before real call, address:", c.address)
			done <- nil
			return
		}

		err := c.rpcClient.Call(method, args, reply)
		done <- err
	}()

	if callTimeout == 0 {
		callTimeout = c.callTimeout
	}

	select {
	case <-time.After(callTimeout):
		log.Printf("[W] rpc call timeout %v => %v", c.rpcClient, c.address)
		return fmt.Errorf("timeout")
	case err := <-done:
		return err
	}

	return nil
}
