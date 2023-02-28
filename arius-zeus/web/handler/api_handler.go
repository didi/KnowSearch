package handler

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"regexp"
	"strconv"
	"strings"
	"time"

	"github.com/gorilla/mux"
	"github.com/toolkits/str"
	"github.com/toolkits/web/param"

	"zeus/web/config"
	"zeus/web/http/render"
	"zeus/web/model"
)

func APITaskCreate(w http.ResponseWriter, r *http.Request) {
	token := param.MustString(r, "token")

	username, err := model.FindToken(token)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if username == "" {
		render.Data(w, "", fmt.Errorf("token not exist"))
		return
	}

	log.Printf("UserInfo, username: %s, token:%s", username, token)

	exists, err := model.UserExists(username)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if !exists {
		render.Data(w, "", fmt.Errorf("Login failed, please contact administrator @zhangzhao @qinxiaohui"))
		return
	}

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	taskInfo := model.TaskCreate{}
	err = json.Unmarshal(body, &taskInfo)
	if err != nil {
		render.Data(w, "", err)
		return
	}
	log.Printf("taskInfo: %v", taskInfo)

	hostnames := ""

	tplId := taskInfo.TplId
	account := taskInfo.Account
	hosts := taskInfo.Hosts
	batch := taskInfo.Batch
	tolerance := taskInfo.Tolerance
	timeout := taskInfo.Timeout
	args := taskInfo.Args
	pause := taskInfo.Pause
	action := taskInfo.Action

	if action == "" {
		action = "start"
	} else {
		if action != "start" && action != "pause" {
			render.Data(w, "", fmt.Errorf("action support start or pause, not support %s", action))
			return
		}
	}

	tpl, err := model.TplGet(tplId)
	if err != nil {
		render.Data(w, "", fmt.Errorf("query tpl:%d occur error %v", tplId, err))
		return
	}

	if tpl == nil {
		render.Data(w, "", fmt.Errorf("tpl not found"))
		return
	}

	// 如果这个tpl的名称包含LINK-，说明是链到了另外一个模板
	if strings.Contains(tpl.Keywords, "LINK-") {
		r := regexp.MustCompile(`.*LINK-(\d+).+`)
		realid, err := strconv.ParseInt(r.FindStringSubmatch(tpl.Keywords)[1], 10, 64)
		if err != nil {
			render.Data(w, "", fmt.Errorf("cannot parse "+tpl.Keywords))
			return
		}

		realtpl, err := model.TplGet(realid)
		if err != nil {
			render.Data(w, "", fmt.Errorf("query real tpl:%d occur error %v", realid, err))
			return
		}

		if realtpl == nil {
			render.Data(w, "", fmt.Errorf("link tpl not found"))
			return
		}

		tpl = realtpl
	}

	if len(hosts) == 0 {
		tplHosts, err := tpl.Hosts()
		if err != nil {
			render.Data(w, "", fmt.Errorf("query tpl hosts %d occur error %v", tplId, err))
			return
		}
		hosts = tplHosts
	}

	// 如果未传入机器列表并且模板中也没有机器列表
	if len(hosts) == 0 {
		render.Data(w, "", fmt.Errorf("host is necessary"))
		return
	}

	if account == "" {
		account = tpl.Account
	}

	if batch == 0 {
		batch = tpl.Batch
	}

	if timeout == 0 {
		timeout = tpl.Timeout
	}

	if tolerance == 0 {
		tolerance = tpl.Tolerance
	}

	if args == "" {
		args = tpl.Args
	}

	if pause == "" {
		pause = tpl.Pause
	}

	task := new(model.TaskMeta)
	task.Creator = username
	task.Created = int(time.Now().Unix())
	task.Keywords = tpl.Keywords
	task.Pause = pause
	task.Script = tpl.Script
	task.Account = account
	task.Batch = batch
	task.Tolerance = tolerance
	task.Timeout = timeout
	task.Args = args
	hostnames = strings.Join(hosts, "\n")

	err = model.TaskMetaInsert(task, hostnames, action)
	render.Data(w, task.ID, err)
}

func APITaskAction(w http.ResponseWriter, r *http.Request) {
	token := param.MustString(r, "token")

	username, err := model.FindToken(token)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if username == "" {
		render.Data(w, "", fmt.Errorf("token not exist"))
		return
	}

	exists, err := model.UserExists(username)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if !exists {
		render.Data(w, "", fmt.Errorf("Login failed, please contact administrator @zhangzhao @qinxiaohui"))
		return
	}

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	taskAction := model.APITaskAction{}
	err = json.Unmarshal(body, &taskAction)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	action := taskAction.Action
	taskId := taskAction.TaskId

	task, err := model.TaskMetaGet(taskId)
	if err != nil {
		render.Data(w, "", err)
		return
	}
	if task == nil {
		render.Data(w, "", fmt.Errorf("任务不存在"))
		return
	}

	hosts, err := task.Hosts()
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if !(action == "start" || action == "pause" || action == "kill" || action == "cancel") {
		render.Data(w, "", fmt.Errorf(action+" not implemented"))
		return
	}

	waitings, err := model.TaskHostRepo.FindByStatus(task.ID, "waiting")
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if action == "start" && len(waitings) != len(hosts) {
		pauseHost, err := model.TaskHostRepo.Get(task.ID, hosts[0])
		if err != nil {
			render.Data(w, "", err)
			return
		}

		if pauseHost.Status == "waiting" || pauseHost.Status == "running" {
			render.Data(w, "", fmt.Errorf("Please wait patiently for the first host to complete！！！"))
			return
		}
	}

	err = model.TaskActionRepo.Update(taskId, action)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	render.Data(w, "", nil)
}

func APITaskResult(w http.ResponseWriter, r *http.Request) {
	task, err := TaskRequired(r)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	hosts, err := model.TaskHostRepo.FindStatus(task.ID)
	if err != nil {
		render.Data(w, "", fmt.Errorf("load task hosts of %d occur error %v", task.ID, err))
		return
	}

	ss := make(map[string][]string)
	total := len(hosts)
	for i := 0; i < total; i++ {
		s := hosts[i].Status
		ss[s] = append(ss[s], hosts[i].Hostname)
	}
	render.Data(w, ss, nil)
}

func APITaskJsonStdouts(w http.ResponseWriter, r *http.Request) {
	task, err := TaskRequired(r)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	hostname := param.String(r, "hostname", "")
	hostname = strings.TrimSpace(hostname)

	var hostsLen int
	ret := []model.TaskStdoutData{}

	if hostname != "" {
		obj, err := model.TaskHostRepo.Get(task.ID, hostname)
		if err != nil {
			render.Data(w, "", err)
			return
		} else if obj == nil {
			render.Data(w, "", fmt.Errorf("task: %d, hostname(%s) not eixsts", task.ID, hostname))
			return
		} else {
			ret = append(ret, model.TaskStdoutData{
				Hostname: hostname,
				Stdout:   obj.Stdout,
			})
		}
	} else {
		hosts, err := model.TaskHostRepo.FindStdout(task.ID)
		if err != nil {
			render.Data(w, "", err)
			return
		}

		hostsLen = len(hosts)

		for i := 0; i < hostsLen; i++ {
			ret = append(ret, model.TaskStdoutData{
				Hostname: hosts[i].Hostname,
				Stdout:   hosts[i].Stdout,
			})
		}
	}

	render.Data(w, ret, nil)
}

func APITaskTxtStdouts(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")

	task, err := TaskRequired(r)
	if err != nil {
		w.Write([]byte("error:" + err.Error()))
		return
	}

	hostname := param.String(r, "hostname", "")
	hostname = strings.TrimSpace(hostname)
	if hostname != "" {
		host, err := model.TaskHostRepo.Get(task.ID, hostname)
		if err != nil {
			w.Write([]byte("error:" + err.Error()))
			return
		} else if host == nil {
			render.Data(w, "", fmt.Errorf("task: %d, hostname(%s) not eixsts", task.ID, hostname))
			return
		}

		w.Write([]byte(host.Stdout))
	} else {
		hosts, err := model.TaskHostRepo.FindStdout(task.ID)
		if err != nil {
			w.Write([]byte("error:" + err.Error()))
			return
		}

		count := len(hosts)
		for i := 0; i < count; i++ {
			if i != 0 {
				w.Write([]byte("\n\n"))
			}

			w.Write([]byte(hosts[i].Hostname + ":\n"))
			w.Write([]byte(hosts[i].Stdout))
		}
	}
}

func APITaskJsonStderrs(w http.ResponseWriter, r *http.Request) {
	task, err := TaskRequired(r)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	hostname := param.String(r, "hostname", "")
	hostname = strings.TrimSpace(hostname)

	var hostsLen int
	ret := []model.TaskStderrData{}

	if hostname != "" {
		obj, err := model.TaskHostRepo.Get(task.ID, hostname)
		if err != nil {
			render.Data(w, "", err)
			return
		} else if obj == nil {
			render.Data(w, "", fmt.Errorf("task: %d, hostname(%s) not eixsts", task.ID, hostname))
			return
		} else {
			ret = append(ret, model.TaskStderrData{
				Hostname: hostname,
				Stderr:   obj.Stderr,
			})
		}
	} else {
		hosts, err := model.TaskHostRepo.FindStdout(task.ID)
		if err != nil {
			render.Data(w, "", err)
			return
		}

		hostsLen = len(hosts)

		for i := 0; i < hostsLen; i++ {
			ret = append(ret, model.TaskStderrData{
				Hostname: hosts[i].Hostname,
				Stderr:   hosts[i].Stderr,
			})
		}
	}

	render.Data(w, ret, nil)
}

func APITaskTxtStderrs(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")

	task, err := TaskRequired(r)
	if err != nil {
		w.Write([]byte("error:" + err.Error()))
		return
	}

	hostname := param.String(r, "hostname", "")
	hostname = strings.TrimSpace(hostname)
	if hostname != "" {
		host, err := model.TaskHostRepo.Get(task.ID, hostname)
		if err != nil {
			w.Write([]byte("error:" + err.Error()))
			return
		} else if host == nil {
			render.Data(w, "", fmt.Errorf("task: %d, hostname(%s) not eixsts", task.ID, hostname))
			return
		}

		w.Write([]byte(host.Stderr))
	} else {
		hosts, err := model.TaskHostRepo.FindStdout(task.ID)
		if err != nil {
			w.Write([]byte("error:" + err.Error()))
			return
		}

		count := len(hosts)
		for i := 0; i < count; i++ {
			if i != 0 {
				w.Write([]byte("\n\n"))
			}

			w.Write([]byte(hosts[i].Hostname + ":\n"))
			w.Write([]byte(hosts[i].Stderr))
		}
	}
}

func APIUserTpls(w http.ResponseWriter, r *http.Request) {
	user := param.MustString(r, "user")

	var grpids []int64
	var ret model.APIUserTpls
	var tpls []model.APIUserTpl

	grps, err := model.GrpAll()
	if err != nil {
		render.Data(w, "", err)
		return
	}

	for i := 0; i < len(grps); i++ {
		has, err := grps[i].HasPriv(user)
		if err != nil {
			render.Data(w, "", err)
			return
		}

		if has {
			grpids = append(grpids, grps[i].ID)
		}
	}

	for i := 0; i < len(grpids); i++ {
		t, err := model.TplList(grpids[i])
		if err != nil {
			render.Data(w, "", err)
			return
		}

		for j := 0; j < len(t); j++ {
			tpl := model.APIUserTpl{
				Id:       t[j].ID,
				Keywords: t[j].Keywords,
			}

			tpls = append(tpls, tpl)
		}
	}

	ret.User = user
	ret.Tpls = tpls

	render.Data(w, ret, nil)
}

func APITaskIsFinish(w http.ResponseWriter, r *http.Request) {
	task := TaskMetaRequired(r)

	action, err := model.TaskActionRepo.Get(task.ID)
	if err != nil {
		render.Message(w, err)
		return
	}

	if action == nil {
		render.Data(w, true, err)
	} else {
		render.Data(w, false, err)
	}
}

func APITaskHostAction(w http.ResponseWriter, r *http.Request) {
	token := param.MustString(r, "token")

	username, err := model.FindToken(token)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if username == "" {
		render.Data(w, "", fmt.Errorf("token not exist"))
		return
	}

	exists, err := model.UserExists(username)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if !exists {
		render.Data(w, "", fmt.Errorf("Login failed, please contact administrator @zhangzhao @qinxiaohui"))
		return
	}

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	hostAction := model.TaskHostAction{}
	err = json.Unmarshal(body, &hostAction)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	task, err := model.TaskMetaGet(hostAction.TaskId)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	if task == nil {
		render.Data(w, "", fmt.Errorf("no task found"))
		return
	}

	NoopWhenDone(task.ID)

	host := hostAction.Hostname

	switch hostAction.Action {
	case "kill":
		render.Data(w, "", task.KillHost(host))
	case "redo":
		render.Data(w, "", task.RedoHost(host))
	case "ignore":
		err = task.IgnoreHost(host)
		if err != nil {
			render.Message(w, err)
			return
		}

		data := "The host has ignored：" + host
		action, err := model.TaskActionRepo.Get(task.ID)
		if err == nil && action != nil {
			if action.Action == "pause" {
				data += "。Tasks are currently paused, if you want to continue, please perform Start operation"
			}
		}
		render.Data(w, data, nil)
	default:
		render.Data(w, "", fmt.Errorf("This operation is not supported"))
	}
}

func APITaskState(w http.ResponseWriter, r *http.Request) {
	task, err := TaskRequired(r)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	action, err := model.TaskActionRepo.Get(task.ID)
	if err != nil {
		render.Data(w, "", err)
		return
	}

	state := "done"
	if action != nil {
		state = action.Action
	}

	render.Data(w, state, nil)
}

func APIGetToken(w http.ResponseWriter, r *http.Request) {
	user := param.MustString(r, "user")
	pass := param.MustString(r, "pass")
	username := param.MustString(r, "username")

	if !config.CheckAuth(user, pass) {
		render.Message(w, fmt.Errorf("FAIL: not auth"))
		return
	}

	token, err := model.GetTokenByUsername(username)
	if err != nil {
		render.Message(w, err)
		return
	}

	if strings.TrimSpace(token) != "" {
		render.Data(w, token, nil)
		return
	}

	token, err = setToken(username)
	if err != nil {
		render.Message(w, err)
		return
	}

	render.Data(w, token, nil)
}

func APIGrpChildren(w http.ResponseWriter, r *http.Request) {
	_, err := TokenRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	idstr, found := mux.Vars(r)["gid"]
	if !found {
		render.Message(w, fmt.Errorf("param error"))
	}

	id, err := strconv.ParseInt(idstr, 10, 64)
	if err != nil {
		render.Message(w, err)
		return
	}

	pGrp, err := model.GrpGet(id)
	if err != nil {
		render.Message(w, err)
		return
	}

	if pGrp == nil {
		render.Message(w, fmt.Errorf("not found grp[%d]", id))
		return
	}

	grps, err := model.GrpList(pGrp.ID)
	if err != nil {
		render.Message(w, err)
		return
	}

	tpls, err := model.TplList(pGrp.ID)
	render.Data(w, map[string]interface{}{
		"grps": grps,
		"tpls": tpls,
	}, err)
}

func APIGrpAdd(w http.ResponseWriter, r *http.Request) {
	username, err := TokenRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	var f model.APIGrpAdd
	err = JsonBind(r, &f)
	if err != nil {
		render.Message(w, err)
		return
	}

	if str.HasDangerousCharacters(f.Name) {
		render.Message(w, fmt.Errorf("%s has dangerous characters", f.Name))
		return
	}

	if str.HasDangerousCharacters(f.Users) {
		render.Message(w, fmt.Errorf("%s has dangerous characters", f.Users))
		return
	}

	if f.Pid > 0 {
		// 非顶层分组，必须具备父分组的权限才能创建
		g, err := model.GrpGet(f.Pid)
		if err != nil {
			render.Message(w, err)
			return
		}

		has, err := g.HasPriv(username)
		if err != nil {
			render.Message(w, err)
			return
		}

		if !has {
			render.Message(w, fmt.Errorf("no privilege"))
			return
		}
	}

	if f.Pid == 0 {
		// 顶层节点，把自己加进去
		arr := strings.Fields(f.Users)
		has := false
		for i := 0; i < len(arr); i++ {
			if arr[i] == username {
				has = true
			}
		}

		if !has {
			f.Users = strings.TrimSpace(f.Users + " " + username)
		}
	}

	err = model.GrpInsert(f.Pid, f.Name, f.Users)
	if err != nil {
		render.Message(w, err)
		return
	}

	g, err := model.GrpGetByNameAndPid(f.Pid, f.Name)
	if err != nil {
		render.Message(w, err)
		return
	}

	render.Data(w, g.ID, nil)
}

func APIGrpDel(w http.ResponseWriter, r *http.Request) {
	g, err := APIGrpPrivRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	render.Message(w, g.Del())
}

func APIGrpUpdate(w http.ResponseWriter, r *http.Request) {
	var f model.APIGrpAdd
	err := JsonBind(r, &f)
	if err != nil {
		render.Message(w, err)
		return
	}

	if str.HasDangerousCharacters(f.Name) {
		render.Message(w, fmt.Errorf("%s has dangerous characters", f.Name))
		return
	}

	if str.HasDangerousCharacters(f.Users) {
		render.Message(w, fmt.Errorf("%s has dangerous characters", f.Users))
		return
	}

	g, err := APIGrpPrivRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	if strings.TrimSpace(f.Name) == "" {
		f.Name = g.Name
	}

	render.Message(w, g.Update(g.PID, f.Name, f.Users))
}

func TplNewFormValidate(f *model.APITpl) error {
	if strings.TrimSpace(f.Script) == "" {
		return fmt.Errorf("script is blank")
	}

	if strings.TrimSpace(f.Keywords) == "" {
		return fmt.Errorf("keywords is blank")
	}

	if strings.TrimSpace(f.Account) == "" {
		return fmt.Errorf("account is blank")
	}

	if f.Timeout == 0 {
		f.Timeout = 30
	}

	return nil
}

func APITplNewPost(w http.ResponseWriter, r *http.Request) {
	username, err := TokenRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	g, err := APIGrpPrivRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	var f model.APITpl
	err = JsonBind(r, &f)
	if err != nil {
		render.Message(w, err)
		return
	}

	err = TplNewFormValidate(&f)
	if err != nil {
		render.Message(w, err)
		return
	}

	err = ScriptSafeRequired(f.Script)
	if err != nil {
		render.Message(w, err)
		return
	}

	tpl := model.Tpl{
		Batch:     f.Batch,
		Tolerance: f.Tolerance,
		Timeout:   f.Timeout,
		Pause:     f.Pause,
		Script:    f.Script,
		Args:      f.Args,
		Updator:   username,
		Updated:   int(time.Now().Unix()),
		Account:   f.Account,
		Keywords:  f.Keywords,
		GID:       g.ID,
	}

	id, err := model.TplInsert(&tpl, f.Hostnames)

	render.Data(w, id, err)
}

func APITplEditPost(w http.ResponseWriter, r *http.Request) {
	tpl, err := APITplRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	username, err := TokenRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	g, err := model.GrpGet(tpl.GID)
	if err != nil {
		render.Message(w, err)
		return
	}

	if g == nil {
		render.Message(w, fmt.Errorf("not found grp[%d]", tpl.GID))
		return
	}

	has, err := g.HasPriv(username)
	if err != nil {
		render.Message(w, err)
		return
	}

	if !has {
		render.Message(w, fmt.Errorf("no privilege"))
		return
	}

	var f model.APITpl
	err = JsonBind(r, &f)
	if err != nil {
		render.Message(w, err)
		return
	}

	err = TplNewFormValidate(&f)
	if err != nil {
		render.Message(w, err)
		return
	}

	err = ScriptSafeRequired(f.Script)
	if err != nil {
		render.Message(w, err)
		return
	}

	target := &model.Tpl{
		Batch:     f.Batch,
		Tolerance: f.Tolerance,
		Timeout:   f.Timeout,
		Pause:     f.Pause,
		Script:    f.Script,
		Args:      f.Args,
		Account:   f.Account,
		Keywords:  f.Keywords,
		Updator:   username,
		Updated:   int(time.Now().Unix()),
	}

	render.Message(w, tpl.Update(target, f.Hostnames))
}

func APITplGet(w http.ResponseWriter, r *http.Request) {
	tpl, err := APITplRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	_, err = TokenRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	hosts, err := tpl.Hosts()
	if err != nil {
		render.Message(w, err)
		return
	}

	data := model.APITpl{
		ID:        tpl.ID,
		GID:       tpl.GID,
		Updator:   tpl.Updator,
		Updated:   tpl.Updated,
		Keywords:  tpl.Keywords,
		Batch:     tpl.Batch,
		Tolerance: tpl.Tolerance,
		Timeout:   tpl.Timeout,
		Pause:     tpl.Pause,
		Script:    tpl.Script,
		Args:      tpl.Args,
		Account:   tpl.Account,
		Hostnames: strings.Join(hosts, "\n"),
	}

	render.Data(w, data, err)
}

func APITplDel(w http.ResponseWriter, r *http.Request) {
	tpl, err := APITplRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	username, err := TokenRequired(r)
	if err != nil {
		render.Message(w, err)
		return
	}

	g, err := model.GrpGet(tpl.GID)
	if err != nil {
		render.Message(w, err)
		return
	}

	if g == nil {
		render.Message(w, fmt.Errorf("not found grp[%d]", tpl.GID))
		return
	}

	has, err := g.HasPriv(username)
	if err != nil {
		render.Message(w, err)
		return
	}

	if !has {
		render.Message(w, fmt.Errorf("no privilege"))
		return
	}

	render.Message(w, tpl.Del())
}
