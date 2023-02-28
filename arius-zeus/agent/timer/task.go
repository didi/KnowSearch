package timer

import (
	"bytes"
	"fmt"
	"log"
	"os/exec"
	"path"
	"strings"
	"sync"

	"github.com/toolkits/file"
	"github.com/toolkits/sys"

	"zeus/agent/client"
	"zeus/agent/config"
	"zeus/agent/utils"
)

// Task 封装一个任务实体
type Task struct {
	sync.Mutex

	ID     int64
	TS     int
	Action string
	Status string

	processAlive bool
	Cmd          *exec.Cmd
	Stdout       bytes.Buffer
	Stderr       bytes.Buffer

	Args    string
	Account string
}

func (t *Task) SetStatus(status string) {
	t.Lock()
	t.Status = status
	t.Unlock()
}

func (t *Task) GetStatus() string {
	t.Lock()
	s := t.Status
	t.Unlock()
	return s
}

func (t *Task) GetProcessAlive() bool {
	t.Lock()
	pa := t.processAlive
	t.Unlock()
	return pa
}

func (t *Task) SetProcessAlive(pa bool) {
	t.Lock()
	t.processAlive = pa
	t.Unlock()
}

// 保证script/args/account等信息都拉取下来了
func (t *Task) prepare() error {
	if t.Account != "" {
		return nil
	}

	IDDir := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID))
	err := file.InsureDir(IDDir)
	if err != nil {
		log.Printf("[E] mkdir -p %s fail: %v\n", IDDir, err)
		return err
	}

	writeFlag := path.Join(IDDir, ".write")
	if file.IsExist(writeFlag) {
		// 从磁盘读取
		argsFile := path.Join(IDDir, "args")
		args, err := file.ToTrimString(argsFile)
		if err != nil {
			log.Printf("[E] read %s fail %v\n", argsFile, err)
			return err
		}

		accountFile := path.Join(IDDir, "account")
		account, err := file.ToTrimString(accountFile)
		if err != nil {
			log.Printf("[E] read %s fail %v\n", accountFile, err)
			return err
		}

		t.Args = args
		t.Account = account
	} else {
		// 从远端读取，再写入磁盘
		script, args, account, err := client.Meta(t.ID)
		if err != nil {
			log.Println("[E] load task meta occur error", err)
			return err
		}

		scriptFile := path.Join(IDDir, "script")
		_, err = file.WriteString(scriptFile, script)
		if err != nil {
			log.Printf("[E] write script to %s fail %v\n", scriptFile, err)
			return err
		}

		out, err := sys.CmdOut("chmod", "+x", scriptFile)
		if err != nil {
			log.Printf("[E] chmod +x %s fail %v. output: %s\n", scriptFile, err, out)
			return err
		}

		argsFile := path.Join(IDDir, "args")
		_, err = file.WriteString(argsFile, args)
		if err != nil {
			log.Printf("[E] write args to %s fail %v\n", argsFile, err)
			return err
		}

		accountFile := path.Join(IDDir, "account")
		_, err = file.WriteString(accountFile, account)
		if err != nil {
			log.Printf("[E] write account to %s fail %v\n", accountFile, err)
			return err
		}

		_, err = file.WriteString(writeFlag, "")
		if err != nil {
			log.Printf("[E] create %s flag file fail %v\n", writeFlag, err)
			return err
		}

		t.Args = args
		t.Account = account
	}

	return nil
}

func (t *Task) start() {
	if t.GetProcessAlive() {
		// 当前有个进程正在跑，可能hang住了，再start也是徒劳的
		return
	}

	err := t.prepare()
	if err != nil {
		return
	}

	args := t.Args
	if args != "" {
		// so，args不支持单引号
		args = strings.Replace(args, ",,", "' '", -1)
		args = " '" + args + "'"
	}

	scriptFile := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), "script")
	sh := fmt.Sprintf("%s%s", scriptFile, args)
	var cmd *exec.Cmd
	if t.Account == "root" {
		cmd = exec.Command("sh", "-c", sh)
		cmd.Dir = "/root"
	} else {
		cmd = exec.Command("su", "-c", sh, "-", t.Account)
	}

	cmd.Stdout = &t.Stdout
	cmd.Stderr = &t.Stderr
	t.Cmd = cmd

	err = cmd.Start()
	if err != nil {
		log.Println("[E] cannot start cmd of task", t.ID)
		return
	}

	go runProcess(t)
}

func (t *Task) kill() {
	go killProcess(t)
}

func (t *Task) doneBefore() bool {
	doneFlag := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), fmt.Sprintf("%d.done", t.TS))
	return file.IsExist(doneFlag)
}

func (t *Task) loadBeforeResult() {
	doneFlag := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), fmt.Sprintf("%d.done", t.TS))
	stdoutFile := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), "stdout")
	stderrFile := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), "stderr")

	var err error

	t.Status, err = file.ToTrimString(doneFlag)
	if err != nil {
		log.Printf("[E] read file %s fail %s", doneFlag, err.Error())
	}
	stdout, err := file.ToString(stdoutFile)
	if err != nil {
		log.Printf("[E] read file %s fail %s", stdoutFile, err.Error())
	}
	stderr, err := file.ToString(stderrFile)
	if err != nil {
		log.Printf("[E] read file %s fail %s", stderrFile, err.Error())
	}

	t.Stdout = *bytes.NewBufferString(stdout)
	t.Stderr = *bytes.NewBufferString(stderr)
}

func runProcess(t *Task) {
	t.SetProcessAlive(true)
	defer t.SetProcessAlive(false)

	err := t.Cmd.Wait()
	if err != nil {
		if strings.Contains(err.Error(), "signal: killed") {
			t.SetStatus("killed")
		} else {
			log.Printf("[E] exec task %d failed %v\n", t.ID, err)
			t.SetStatus("failed")
		}
	} else {
		t.SetStatus("success")
	}

	persistResult(t)
}

func persistResult(t *Task) {
	stdout := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), "stdout")
	stderr := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), "stderr")
	doneFlag := path.Join(config.G.Metadir, fmt.Sprintf("%d", t.ID), fmt.Sprintf("%d.done", t.TS))

	file.WriteString(stdout, t.GetStdout())
	file.WriteString(stderr, t.GetStderr())
	file.WriteString(doneFlag, t.GetStatus())
}

func killProcess(t *Task) {
	t.SetProcessAlive(true)
	defer t.SetProcessAlive(false)

	err := utils.KillProcessByTaskID(t.ID)

	if err != nil {
		log.Printf("[E] kill process of task %d fail %v", t.ID, err)
		t.SetStatus("killfailed")
	} else {
		t.SetStatus("killed")
	}

	persistResult(t)
}

func (t *Task) GetStdout() string {
	t.Lock()
	defer t.Unlock()
	return t.Stdout.String()
}

func (t *Task) GetStderr() string {
	t.Lock()
	defer t.Unlock()
	return t.Stderr.String()
}

func (t *Task) ResetBuff() {
	t.Lock()
	defer t.Unlock()
	t.Stdout.Reset()
	t.Stderr.Reset()
}
