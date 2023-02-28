package model

type TaskCreate struct {
	TplId     int64    `json:"tpl_id"`
	Account   string   `json:"account"`
	Hosts     []string `json:"hosts"`
	Batch     int      `json:"batch"`
	Tolerance int      `json:"tolerance"`
	Pause     string   `json:"pause"`
	Timeout   int      `json:"timeout"`
	Args      string   `json:"args"`
	Action    string   `json:"action"`
}

type TaskResult struct {
	Success    []string `json:"success"`
	Failed     []string `json:"failed"`
	Running    []string `json:"running"`
	Waiting    []string `json:"waiting"`
	Timeout    []string `json:"timeout"`
	Killing    []string `json:"killing"`
	KillFailed []string `json:"kill_failed"`
}

type TaskStdoutData struct {
	Hostname string `json:"hostname"`
	Stdout   string `json:"stdout"`
}

type TaskStderrData struct {
	Hostname string `json:"hostname"`
	Stderr   string `json:"stderr"`
}

type APITaskAction struct {
	TaskId int64  `json:"task_id"`
	Action string `json:"action"`
}

type APIUserTpls struct {
	User string       `json:"user"`
	Tpls []APIUserTpl `json:"tpls"`
}

type APIUserTpl struct {
	Id       int64  `json:"id"`
	Keywords string `json:"keywords"`
}

type TaskHostAction struct {
	TaskId   int64  `json:"task_id"`
	Hostname string `json:"hostname"`
	Action   string `json:"action"`
}

type APIGrpAdd struct {
	Pid   int64  `json:"pid"`
	Users string `json:"users"`
	Name  string `json:"name"`
}

type APITpl struct {
	ID        int64  `json:"id"`
	GID       int64  `json:"gid"`
	Updator   string `json:"updator"`
	Updated   int    `json:"updated"`
	Keywords  string `json:"keywords"`
	Batch     int    `json:"batch"`
	Tolerance int    `json:"tolerance"`
	Timeout   int    `json:"timeout"`
	Pause     string `json:"pause"`
	Script    string `json:"script"`
	Args      string `json:"args"`
	Account   string `json:"account"`
	Hostnames string `json:"hostnames"`
}
