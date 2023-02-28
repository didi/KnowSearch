package handler

import (
	"fmt"
	"net/http"
	"regexp"
	"strconv"
	"strings"
	"time"

	"github.com/toolkits/web"
	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/http/render"
	"zeus/web/model"
	"zeus/web/utils"
)

func TaskNewGet(w http.ResponseWriter, r *http.Request) {
	loginUser := UsernameRequired(r)

	id := param.Int64(r, "fork", 0)
	tplId := param.Int64(r, "tpl", 0)

	hostnames := ""

	task := new(model.TaskMeta)
	task.Creator = loginUser
	task.Keywords = "tmp task"
	task.Account = "root"
	task.Timeout = 30
	task.Script = `#!/bin/sh
# e.g.
export PATH=/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:~/bin
ss -tln

`

	if id > 0 {
		var err error
		task, err = model.TaskMetaGet(id)
		if err != nil {
			http.Error(w, fmt.Sprintf("query task meta %d occur error %v", id, err), 200)
			return
		}

		if task == nil {
			http.Error(w, "fork target not found", 200)
			return
		}

		hosts, err := task.Hosts()
		if err != nil {
			http.Error(w, fmt.Sprintf("query task hosts %d occur error %v", id, err), 200)
			return
		}

		hostnames = strings.Join(hosts, "\n")
	}

	if tplId > 0 {
		tpl, err := model.TplGet(tplId)
		if err != nil {
			http.Error(w, fmt.Sprintf("query tpl:%d occur error %v", tplId, err), 200)
			return
		}

		if tpl == nil {
			http.Error(w, "tpl not found", 200)
			return
		}

		// 如果这个tpl的名称包含LINK-，说明是链到了另外一个模板
		if strings.Contains(tpl.Keywords, "LINK-") {
			r := regexp.MustCompile(`.*LINK-(\d+).+`)
			realid, err := strconv.ParseInt(r.FindStringSubmatch(tpl.Keywords)[1], 10, 64)
			if err != nil {
				http.Error(w, "cannot parse "+tpl.Keywords, 200)
				return
			}

			realtpl, err := model.TplGet(realid)
			if err != nil {
				http.Error(w, fmt.Sprintf("query real tpl:%d occur error %v", realid, err), 200)
				return
			}

			if realtpl == nil {
				http.Error(w, "link tpl not found", 200)
				return
			}

			tpl = realtpl
		}

		hosts, err := tpl.Hosts()
		if err != nil {
			http.Error(w, fmt.Sprintf("query tpl hosts %d occur error %v", tplId, err), 200)
			return
		}

		task.Keywords = tpl.Keywords
		task.Batch = tpl.Batch
		task.Tolerance = tpl.Tolerance
		task.Timeout = tpl.Timeout
		task.Pause = tpl.Pause
		task.Script = tpl.Script
		task.Args = tpl.Args
		task.Account = tpl.Account

		hostnames = strings.Join(hosts, "\n")
		hostnames = strings.Join(utils.ParseLines(hostnames), "\n")
	}

	render.Put(r, "Task", task)
	render.Put(r, "Hosts", hostnames)
	render.HTML(r, w, "task/editor")
}

// 任务创建出来之后不会立马执行，先进入detail页面查看，检查没问题了，点击开始执行
func TaskNewPost(w http.ResponseWriter, r *http.Request) {
	batch := param.Int(r, "batch", 0)
	tolerance := param.Int(r, "tolerance", 0)
	timeout := param.Int(r, "timeout", 30)
	pause := param.String(r, "pause", "")
	hostnames := param.MustString(r, "hostnames")
	script := param.MustString(r, "script")
	args := param.String(r, "args", "")
	account := param.MustString(r, "account")
	keywords := param.MustString(r, "keywords")
	action := param.MustString(r, "action")

	loginUser := UsernameRequired(r)

	err := ScriptSafeRequired(script)
	errors.MaybePanic(err)

	meta := model.TaskMeta{
		Batch:     batch,
		Tolerance: tolerance,
		Timeout:   timeout,
		Pause:     pause,
		Script:    script,
		Args:      args,
		Creator:   loginUser,
		Created:   int(time.Now().Unix()),
		Account:   account,
		Keywords:  keywords,
	}

	err = model.TaskMetaInsert(&meta, hostnames, action)
	render.Data(w, meta.ID, err)
}

// TaskResult 展示某一个任务的结果详情
func TaskResult(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)
	task := TaskMetaRequired(r)

	hosts, err := model.TaskHostRepo.FindStatus(task.ID)
	if err != nil {
		panic(errors.InternalServerError(fmt.Sprintf("load task hosts of %d occur error %v", task.ID, err)))
	}

	action, err := model.TaskActionRepo.Get(task.ID)
	errors.MaybePanic(err)

	if action == nil {
		// 任务已经完成了，不能再控制了
		render.Put(r, "IsDone", true)
	} else {
		render.Put(r, "IsDone", false)
		render.Put(r, "IsPause", action.Action == "pause")
	}

	// 把各个状态的机器数目做个统计
	ss := make(map[string]int)
	total := len(hosts)
	for i := 0; i < total; i++ {
		s := hosts[i].Status
		if _, found := ss[s]; found {
			ss[s]++
		} else {
			ss[s] = 1
		}
	}

	// 只展示指定status的机器
	status := param.String(r, "status", "")
	if status == "" {
		render.Put(r, "Hosts", hosts)
	} else {
		hs := make([]model.TaskHost, 0, total)
		for i := 0; i < total; i++ {
			if hosts[i].Status == status {
				hs = append(hs, hosts[i])
			}
		}
		render.Put(r, "Hosts", hs)
	}

	render.Put(r, "Task", task)
	render.Put(r, "Ss", ss)
	render.Put(r, "Total", total)
	render.HTML(r, w, "task/result")
}

func TaskDetail(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)
	task := TaskMetaRequired(r)
	hosts, err := model.TaskHostRepo.FindStatus(task.ID)
	if err != nil {
		panic(errors.InternalServerError(fmt.Sprintf("load task hosts of %d occur error %v", task.ID, err)))
	}

	action, err := model.TaskActionRepo.Get(task.ID)
	errors.MaybePanic(err)

	if action == nil {
		render.Put(r, "IsFinished", true)
	} else {
		render.Put(r, "IsFinished", false)
	}

	render.Put(r, "Task", task)
	render.Put(r, "Hosts", hosts)
	render.Put(r, "TaskHistory", true)
	render.HTML(r, w, "task/detail")
}

func TaskList(w http.ResponseWriter, r *http.Request) {
	loginUser := UsernameRequired(r)

	mine := param.Int(r, "mine", 1)
	query := param.String(r, "query", "")
	limit := param.Int(r, "limit", 20)

	creator := loginUser
	if mine == 0 {
		creator = ""
	}

	total, err := model.TaskMetaCount(creator, query)
	errors.MaybePanic(err)

	pager := web.NewPaginator(r, limit, total)

	tasks, err := model.TaskMetaList(creator, query, limit, pager.Offset())
	errors.MaybePanic(err)

	render.Put(r, "Mine", mine)
	render.Put(r, "Pager", pager)
	render.Put(r, "Tasks", tasks)
	render.Put(r, "Query", query)
	render.HTML(r, w, "task/list")
}

func TaskAction(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)
	task := TaskMetaRequired(r)

	hosts, err := task.Hosts()
	errors.MaybePanic(err)

	action := param.MustString(r, "action")

	if !(action == "start" || action == "pause" || action == "kill" || action == "cancel") {
		panic(errors.BadRequestError(action + " not implemented"))
	}

	if action == "cancel" {
		render.Message(w, model.TaskActionRepo.Update(task.ID, action))
		return
	}

	waitings, err := model.TaskHostRepo.FindByStatus(task.ID, "waiting")
	errors.MaybePanic(err)

	// 只关注强制暂停点的情况，判断第一台机器是否执行完成
	if action == "start" && len(waitings) != len(hosts) {
		pauseHost, err := model.TaskHostRepo.Get(task.ID, hosts[0])
		errors.MaybePanic(err)

		if pauseHost.Status == "waiting" || pauseHost.Status == "running" {
			errors.Dangerous("请耐心等待第一台机器执行完成！！！")
		}
	}

	render.Message(w, model.TaskActionRepo.Update(task.ID, action))
}

func TaskStatusHosts(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)
	status := StatusRequired(r)
	task := TaskMetaRequired(r)

	hosts, err := model.TaskHostRepo.FindByStatus(task.ID, status)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	count := len(hosts)
	hns := make([]string, 0, count)
	for _, h := range hosts {
		hns = append(hns, h.Hostname)
	}

	render.Text(w, strings.Join(hns, "\n"))
}
