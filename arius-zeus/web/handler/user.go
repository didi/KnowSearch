package handler

import (
	"net/http"

	"github.com/toolkits/web"
	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/http/render"
	"zeus/web/model"
)

func UserList(w http.ResponseWriter, r *http.Request) {
	MaintainerRequired(r)

	query := param.String(r, "query", "")
	limit := param.Int(r, "limit", 20)

	total, err := model.UserCount(query)
	errors.MaybePanic(err)

	pager := web.NewPaginator(r, limit, total)

	users, err := model.UserList(query, limit, pager.Offset())
	errors.MaybePanic(err)

	render.Put(r, "Pager", pager)
	render.Put(r, "Users", users)
	render.Put(r, "Query", query)
	render.HTML(r, w, "user/list")
}

func UserDel(w http.ResponseWriter, r *http.Request) {
	MaintainerRequired(r)

	username := param.MustString(r, "username")

	err := model.UserDel(username)
	render.Message(w, err)
}
