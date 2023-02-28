package rpc

func (*Exec) Ping(input string, output *string) error {
	*output = "pong"
	return nil
}
