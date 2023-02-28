package handler

import (
	"fmt"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/http/render"
	"zeus/web/model"
)

func TplNewGet(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)

	tpl := &model.Tpl{
		Timeout: 30,
		Script: `#!/bin/sh
# e.g.
export PATH=/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:~/bin
ss -tln

`,
	}

	grp := GrpPrivRequired(r)

	render.Put(r, "Obj", tpl)
	render.Put(r, "Grp", grp)
	render.HTML(r, w, "tpl/new")
}

func TplNewPost(w http.ResponseWriter, r *http.Request) {
	batch := param.Int(r, "batch", 0)
	tolerance := param.Int(r, "tolerance", 0)
	timeout := param.Int(r, "timeout", 30)
	pause := param.String(r, "pause", "")
	hostnames := param.String(r, "hostnames", "")
	script := param.MustString(r, "script")
	args := param.String(r, "args", "")
	keywords := param.MustString(r, "keywords")
	account := param.MustString(r, "account")

	loginUser := UsernameRequired(r)
	g := GrpPrivRequired(r)

	err := ScriptSafeRequired(script)
	errors.MaybePanic(err)

	tpl := model.Tpl{
		Batch:     batch,
		Tolerance: tolerance,
		Timeout:   timeout,
		Pause:     pause,
		Script:    script,
		Args:      args,
		Updator:   loginUser,
		Updated:   int(time.Now().Unix()),
		Account:   account,
		Keywords:  keywords,
		GID:       g.ID,
	}

	_, err = model.TplInsert(&tpl, hostnames)
	render.Message(w, err)
}

func TplEditGet(w http.ResponseWriter, r *http.Request) {
	tpl := TplRequired(r)

	hosts, err := tpl.Hosts()
	errors.MaybePanic(err)

	grp, err := model.GrpGet(tpl.GID)
	errors.MaybePanic(err)

	render.Put(r, "Obj", tpl)
	render.Put(r, "Grp", grp)
	render.Put(r, "Hosts", strings.Join(hosts, "\n"))
	render.HTML(r, w, "tpl/edit")
}

func TplEditPost(w http.ResponseWriter, r *http.Request) {
	tpl := TplRequired(r)
	loginUser := UsernameRequired(r)

	g, err := model.GrpGet(tpl.GID)
	errors.MaybePanic(err)

	has, err := g.HasPriv(loginUser)
	errors.MaybePanic(err)

	if !has {
		errors.Dangerous("no privilege")
	}

	batch := param.Int(r, "batch", 0)
	tolerance := param.Int(r, "tolerance", 0)
	timeout := param.Int(r, "timeout", 30)
	pause := param.String(r, "pause", "")
	hostnames := param.String(r, "hostnames", "")
	script := param.MustString(r, "script")
	args := param.String(r, "args", "")
	keywords := param.MustString(r, "keywords")
	account := param.MustString(r, "account")

	target := &model.Tpl{
		Batch:     batch,
		Tolerance: tolerance,
		Timeout:   timeout,
		Pause:     pause,
		Script:    script,
		Args:      args,
		Account:   account,
		Keywords:  keywords,
		Updator:   loginUser,
		Updated:   int(time.Now().Unix()),
	}

	render.Message(w, tpl.Update(target, hostnames))
}

func TplView(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)

	tpl := TplRequired(r)
	hosts, err := tpl.Hosts()
	errors.MaybePanic(err)

	grp, err := model.GrpGet(tpl.GID)
	errors.MaybePanic(err)

	render.Put(r, "Obj", tpl)
	render.Put(r, "Grp", grp)
	render.Put(r, "Hosts", strings.Join(hosts, "\n"))
	render.HTML(r, w, "tpl/view")
}

func TplDel(w http.ResponseWriter, r *http.Request) {
	tpl := TplRequired(r)
	loginUser := UsernameRequired(r)

	g, err := model.GrpGet(tpl.GID)
	errors.MaybePanic(err)

	has, err := g.HasPriv(loginUser)
	errors.MaybePanic(err)

	if !has {
		errors.Dangerous("no privilege")
	}

	render.Message(w, tpl.Del())
}

func TplMove(w http.ResponseWriter, r *http.Request) {
	tplIds := strings.Split(param.MustString(r, "tpl_ids"), ",")
	grpId := param.MustInt64(r, "grp_id")

	count := len(tplIds)
	tpls := make([]int64, count)
	for i := 0; i < count; i++ {
		tplId, err := strconv.ParseInt(tplIds[i], 10, 64)
		if err != nil {
			errors.Dangerous("")
		}
		tpls[i] = tplId
	}

	//判断当前tpl所在的组有没有权限操作
	tpl, err := model.TplGet(tpls[0])
	errors.MaybePanic(err)
	if grpId == tpl.GID {
		errors.Dangerous(fmt.Sprintf("模板就在%d组id下", grpId))
	}
	GrpMovePrivRequired(r, tpl.GID)

	// 判断目标组有没有权限操作
	GrpMovePrivRequired(r, grpId)

	render.Message(w, model.TplUpdateGID(tpls, grpId))
}
