package handler

import (
	"net/http"

	"github.com/toolkits/web/param"

	"zeus/web/http/render"
	"zeus/web/model"
)

func StdoutHtml(w http.ResponseWriter, r *http.Request) {
	id := param.MustInt64(r, "id")
	hostname := param.MustString(r, "hostname")
	obj, err := model.TaskHostRepo.Get(id, hostname)
	content := ""
	if err != nil {
		content = "get stdout occur error: " + err.Error()
	} else {
		content = obj.Stdout
	}
	render.Put(r, "Content", content)
	render.HTML(r, w, "task/output")
}

func StderrHtml(w http.ResponseWriter, r *http.Request) {
	id := param.MustInt64(r, "id")
	hostname := param.MustString(r, "hostname")
	obj, err := model.TaskHostRepo.Get(id, hostname)
	content := ""
	if err != nil {
		content = "get stderr occur error: " + err.Error()
	} else {
		content = obj.Stderr
	}
	render.Put(r, "Content", content)
	render.HTML(r, w, "task/output")
}

func StdoutsTxt(w http.ResponseWriter, r *http.Request) {
	task := TaskMetaRequired(r)
	hosts, err := model.TaskHostRepo.FindStdout(task.ID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	count := len(hosts)
	for i := 0; i < count; i++ {
		if i != 0 {
			w.Write([]byte("\n\n"))
		}

		w.Write([]byte(hosts[i].Hostname + ":\n"))
		w.Write([]byte(hosts[i].Stdout))
	}
}
