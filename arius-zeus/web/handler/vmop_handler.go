package handler

import (
	"net/http"

	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/config"
	"zeus/web/http/render"
	"zeus/web/model"
	"zeus/web/utils"
)

func VmopGet(w http.ResponseWriter, r *http.Request) {
	username := UsernameRequired(r)
	if !config.IsMaintainer(username) {
		panic(errors.NoPrivError())
	}

	render.HTML(r, w, "vmop/input")
}

func VmopPost(w http.ResponseWriter, r *http.Request) {
	username := UsernameRequired(r)
	if !config.IsMaintainer(username) {
		panic(errors.NoPrivError())
	}

	hostnames := param.MustString(r, "hostnames")
	usernames := param.MustString(r, "usernames")

	render.Message(w, model.VmopRepo.Insert(utils.ParseLines(hostnames), utils.ParseLines(usernames)))
}
