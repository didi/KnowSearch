package handler

import (
	"fmt"
	"net/http"

	"github.com/toolkits/web"
	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/http/render"
	"zeus/web/model"
)

func AgentList(w http.ResponseWriter, r *http.Request) {
	// loginUser := UsernameRequired(r)
	query := param.String(r, "query", "")
	limit := param.Int(r, "limit", 20)

	total, err := model.EdgeAgentCount(query)
	errors.MaybePanic(err)

	pager := web.NewPaginator(r, limit, total)

	agents, err := model.EdgeAgentList(query, limit, pager.Offset())
	errors.MaybePanic(err)

	render.Put(r, "Pager", pager)
	render.Put(r, "Agents", agents)
	render.Put(r, "Query", query)
	render.HTML(r, w, "agent/list")
}

func ApiAgentList(w http.ResponseWriter, r *http.Request) {
	// loginUser := UsernameRequired(r)
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

	query := param.String(r, "query", "")
	limit := param.Int(r, "limit", 20)
	page := param.Int(r, "page", 1)

	if limit <= 0 {
		render.Data(w, "", fmt.Errorf("param limit <= 0"))
		return
	}

	if page <= 0 {
		render.Data(w, "", fmt.Errorf("param page < 0"))
		return
	}

	total, err := model.EdgeAgentCount(query)
	errors.MaybePanic(err)

	agents, err := model.EdgeAgentList(query, limit, (page-1)*limit)
	errors.MaybePanic(err)

	ret := new(model.EdgeInfoInter)
	ret.Dat = agents
	ret.Total = total
	render.Data(w, ret, err)
}
