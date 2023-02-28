package http

import (
	"fmt"
	"net/http"

	"github.com/gorilla/mux"

	"zeus/web/config"
	"zeus/web/handler"
)

func ConfigRoutes(r *mux.Router) {
	r.HandleFunc("/ping", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "pong")
	})

	r.HandleFunc("/version", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, config.Version)
	})

	r.HandleFunc("/ip", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, r.RemoteAddr)
	})

	r.PathPrefix("/static").Handler(http.FileServer(http.Dir("./")))
	r.HandleFunc("/favicon.ico", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "./static/favicon.ico")
	})

	r.HandleFunc("/", handler.HomeIndex).Methods("GET")
	r.HandleFunc("/login", handler.Login).Methods("POST")
	r.HandleFunc("/logout", handler.Logout)
	r.HandleFunc("/about", handler.About)
	r.HandleFunc("/grps", handler.GrpsGet)
	r.HandleFunc("/grp/add", handler.GrpAdd).Methods("POST")
	r.HandleFunc("/grp/{gid}", handler.GrpDel).Methods("DELETE")
	r.HandleFunc("/grp/{gid}/update", handler.GrpUpdate).Methods("POST")
	r.HandleFunc("/grp/{gid}/children", handler.GrpChildren).Methods("GET")
	r.HandleFunc("/grp/{gid}/tpl/new", handler.TplNewGet).Methods("GET")
	r.HandleFunc("/grp/{gid}/tpl/new", handler.TplNewPost).Methods("POST")
	r.HandleFunc("/tpl/{id}/view", handler.TplView).Methods("GET")
	r.HandleFunc("/tpl/{id}/edit", handler.TplEditGet).Methods("GET")
	r.HandleFunc("/tpl/{id}/edit", handler.TplEditPost).Methods("POST")
	r.HandleFunc("/tpl/{id}", handler.TplDel).Methods("DELETE")
	r.HandleFunc("/tpl/move", handler.TplMove).Methods("POST")
	r.HandleFunc("/task/new", handler.TaskNewGet).Methods("GET")
	r.HandleFunc("/task/new", handler.TaskNewPost).Methods("POST")
	r.HandleFunc("/tasks", handler.TaskList).Methods("GET")

	r.HandleFunc("/task/{id}/detail", handler.TaskDetail).Methods("GET")
	r.HandleFunc("/task/{id}/result", handler.TaskResult).Methods("GET")
	r.HandleFunc("/task/{id}/action", handler.TaskAction).Methods("POST")
	r.HandleFunc("/task/{id}/{status}", handler.TaskStatusHosts).Methods("GET")
	r.HandleFunc("/task/{id}/kill", handler.KillHost).Methods("POST")
	r.HandleFunc("/task/{id}/redo", handler.RedoHost).Methods("POST")
	r.HandleFunc("/task/{id}/ignore", handler.IgnoreHost).Methods("POST")

	// r.HandleFunc("/api/tpl/{id}/run", handler.TemplateRunPostAPI).Methods("POST")
	// r.HandleFunc("/api/tpl/{id}", handler.TemplateGetAPI).Methods("GET")
	// r.HandleFunc("/api/task/{id}/result", handler.APITaskResult).Methods("GET")
	// r.HandleFunc("/api/task/{id}/isfinish", handler.APITaskIsFinish).Methods("GET")

	r.HandleFunc("/vmop", handler.VmopGet).Methods("GET")
	r.HandleFunc("/vmop", handler.VmopPost).Methods("POST")

	r.HandleFunc("/output/stdout.html", handler.StdoutHtml).Methods("GET")
	r.HandleFunc("/output/stderr.html", handler.StderrHtml).Methods("GET")
	r.HandleFunc("/output/stdouts/{id}.txt", handler.StdoutsTxt).Methods("GET")

	r.HandleFunc("/token/list", handler.TokenList).Methods("GET")
	r.HandleFunc("/token/set", handler.SetToken).Methods("GET")

	r.HandleFunc("/api/task", handler.APITaskCreate).Methods("POST")
	r.HandleFunc("/api/task/action", handler.APITaskAction).Methods("POST")
	r.HandleFunc("/api/task/host-action", handler.APITaskHostAction).Methods("POST")
	r.HandleFunc("/api/task/{id}/result", handler.APITaskResult).Methods("GET")
	r.HandleFunc("/api/task/{id}/stdouts.json", handler.APITaskJsonStdouts).Methods("GET")
	r.HandleFunc("/api/task/{id}/stdouts.txt", handler.APITaskTxtStdouts).Methods("GET")
	r.HandleFunc("/api/task/{id}/stderrs.json", handler.APITaskJsonStderrs).Methods("GET")
	r.HandleFunc("/api/task/{id}/stderrs.txt", handler.APITaskTxtStderrs).Methods("GET")
	r.HandleFunc("/api/task/{id}/state", handler.APITaskState).Methods("GET")
	r.HandleFunc("/api/user/tpls", handler.APIUserTpls).Methods("GET")
	r.HandleFunc("/api/token", handler.APIGetToken).Methods("GET")
	r.HandleFunc("/api/grp/{gid}/children", handler.APIGrpChildren).Methods("GET")
	r.HandleFunc("/api/grp/add", handler.APIGrpAdd).Methods("POST")
	r.HandleFunc("/api/grp/{gid}", handler.APIGrpDel).Methods("DELETE")
	r.HandleFunc("/api/grp/{gid}/update", handler.APIGrpUpdate).Methods("POST")
	r.HandleFunc("/api/grp/{gid}/tpl/new", handler.APITplNewPost).Methods("POST")
	r.HandleFunc("/api/tpl/{id}", handler.APITplGet).Methods("GET")
	r.HandleFunc("/api/tpl/{id}/edit", handler.APITplEditPost).Methods("POST")
	r.HandleFunc("/api/tpl/{id}", handler.APITplDel).Methods("DELETE")
	r.HandleFunc("/agents", handler.AgentList).Methods("GET")
	r.HandleFunc("/api/agents-list", handler.ApiAgentList).Methods("GET")
}
