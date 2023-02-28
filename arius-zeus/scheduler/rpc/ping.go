package rpc

func (*Scheduler) Ping(input string, output *string) error {
	*output = "pong"
	return nil
}
