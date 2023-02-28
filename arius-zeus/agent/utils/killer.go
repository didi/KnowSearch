package utils

import (
	"errors"
	"fmt"
	"log"
	"strconv"
	"strings"

	"github.com/toolkits/file"
	"github.com/toolkits/sys"

	"zeus/agent/config"
)

func PidsByCmdline(cmdline string) []int {
	ret := []int{}

	var dirs []string
	dirs, err := file.DirsUnder("/proc")
	if err != nil {
		return ret
	}

	count := len(dirs)
	for i := 0; i < count; i++ {
		pid, err := strconv.Atoi(dirs[i])
		if err != nil {
			continue
		}

		cmdlineFile := fmt.Sprintf("/proc/%d/cmdline", pid)
		if !file.IsExist(cmdlineFile) {
			continue
		}

		cmdlineBytes, err := file.ToBytes(cmdlineFile)
		if err != nil {
			continue
		}

		cmdlineBytesLen := len(cmdlineBytes)
		if cmdlineBytesLen == 0 {
			continue
		}

		noNut := make([]byte, 0, cmdlineBytesLen)
		for j := 0; j < cmdlineBytesLen; j++ {
			if cmdlineBytes[j] != 0 {
				noNut = append(noNut, cmdlineBytes[j])
			}
		}

		if strings.Contains(string(noNut), cmdline) {
			ret = append(ret, pid)
		}
	}

	return ret
}

func KillProcessByCmdline(cmdline string) error {
	cmdline = strings.TrimSpace(cmdline)
	if cmdline == "" {
		return errors.New("cmdline is blank")
	}

	pids := PidsByCmdline(cmdline)
	for i := 0; i < len(pids); i++ {
		out, err := sys.CmdOut("kill", "-9", strconv.Itoa(pids[i]))
		log.Printf("[I] kill -9 %d, output: %s, err: %v\n", pids[i], out, err)
		if err != nil {
			return err
		}
	}

	return nil
}

func KillProcessByTaskID(id int64) error {
	dir := strings.TrimRight(config.G.Metadir, "/")
	arr := strings.Split(dir, "/")
	lst := arr[len(arr)-1]
	return KillProcessByCmdline(fmt.Sprintf("%s/%d/script", lst, id))
}
