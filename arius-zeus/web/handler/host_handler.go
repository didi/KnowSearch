package handler

import (
	"net/http"

	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/http/render"
	"zeus/web/model"
)

func KillHost(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)
	task := TaskMetaRequired(r)
	NoopWhenDone(task.ID)

	host := param.MustString(r, "host")

	render.Message(w, task.KillHost(host))
}

func RedoHost(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)
	task := TaskMetaRequired(r)
	NoopWhenDone(task.ID)

	host := param.MustString(r, "host")

	render.Message(w, task.RedoHost(host))
}

func IgnoreHost(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)
	task := TaskMetaRequired(r)
	NoopWhenDone(task.ID)

	host := param.MustString(r, "host")

	err := task.IgnoreHost(host)
	if err != nil {
		render.Message(w, err)
		return
	}

	data := "系统已忽略该机器：" + host
	action, err := model.TaskActionRepo.Get(task.ID)
	if err == nil && action != nil {
		if action.Action == "pause" {
			data += "。任务当前是暂停状态，如要继续请点击表格上面的Start按钮"
		}
	}

	render.Data(w, data, nil)
}

func NoopWhenDone(id int64) {
	action, err := model.TaskActionRepo.Get(id)
	errors.MaybePanic(err)

	if action == nil {
		errors.Panic("任务已经结束，不能再操作了")
	}
}
