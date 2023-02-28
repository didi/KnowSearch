package handler

import (
	"net/http"
	"strings"

	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/http/render"
	"zeus/web/model"
)

func About(w http.ResponseWriter, r *http.Request) {
	render.HTML(r, w, "mess/about")
}

// GrpsGet 首页展示顶层分组
func GrpsGet(w http.ResponseWriter, r *http.Request) {
	UsernameRequired(r)

	pid := param.Int64(r, "pid", 0)
	search := param.String(r, "search", "")

	list, err := model.GrpSearch(pid, search)
	errors.MaybePanic(err)

	render.Put(r, "Search", search)
	render.Put(r, "List", list)
	render.HTML(r, w, "mess/grps")
}

func GrpAdd(w http.ResponseWriter, r *http.Request) {
	pid := param.MustInt64(r, "pid")
	name := param.MustString(r, "name")
	users := param.String(r, "users", "")

	CheckDangerous("name", name)
	CheckDangerous("users", users)

	loginUser := UsernameRequired(r)

	if pid > 0 {
		// 非顶层分组，必须具备父分组的权限才能创建
		g, err := model.GrpGet(pid)
		errors.MaybePanic(err)

		has, err := g.HasPriv(loginUser)
		errors.MaybePanic(err)

		if !has {
			errors.Dangerous("no privilege")
		}
	}

	if pid == 0 {
		// 顶层节点，把自己加进去
		arr := strings.Fields(users)
		has := false
		for i := 0; i < len(arr); i++ {
			if arr[i] == loginUser {
				has = true
			}
		}

		if !has {
			users = strings.TrimSpace(users + " " + loginUser)
		}
	}

	render.Message(w, model.GrpInsert(pid, name, users))
}

func GrpDel(w http.ResponseWriter, r *http.Request) {
	render.Message(w, GrpPrivRequired(r).Del())
}

func GrpUpdate(w http.ResponseWriter, r *http.Request) {
	g := GrpPrivRequired(r)

	pid := param.MustInt64(r, "pid")
	name := param.MustString(r, "name")
	users := param.String(r, "users", "")

	CheckDangerous("name", name)
	CheckDangerous("users", users)

	if pid == 0 {
		if strings.TrimSpace(users) == "" {
			errors.Dangerous("PID=0，即把当前分组作为顶层分组，则必须指定权限人员")
		}

		loginUser := UsernameRequired(r)
		if !strings.Contains(users, loginUser) {
			users = users + " " + loginUser
		}
	}

	if g.ID == pid {
		errors.Dangerous("父id和组id相同")
	}
	if pid != 0 {
		GrpMovePrivRequired(r, pid)
	}

	render.Message(w, g.Update(pid, name, users))
}

// GrpChildren 展示子分组、直属模板
func GrpChildren(w http.ResponseWriter, r *http.Request) {
	pGrp := GrpRequired(r)

	grps, err := model.GrpList(pGrp.ID)
	errors.MaybePanic(err)

	tpls, err := model.TplList(pGrp.ID)
	errors.MaybePanic(err)

	render.Put(r, "Grps", grps)
	render.Put(r, "Tpls", tpls)
	render.Put(r, "Grp", pGrp)
	render.Put(r, "GrpCount", len(grps))
	render.Put(r, "TplCount", len(tpls))
	render.HTML(r, w, "mess/children")
}
