package handler

import (
	"fmt"
	"net/http"
	"regexp"
	"strconv"
	"strings"

	"github.com/gorilla/mux"
	"github.com/toolkits/web/errors"

	"zeus/web/config"
	"zeus/web/http/cookie"
	"zeus/web/model"
)

// UsernameRequired 要求用户必须登录，否则跳转登录页面
func UsernameRequired(r *http.Request) string {
	username := cookie.ReadUser(r)
	if username == "" {
		panic(errors.NotLoginError())
	}

	exists, err := model.UserExists(username)
	errors.MaybePanic(err)

	if !exists {
		errors.MaybePanic(fmt.Errorf("无权限，请联系管理员 @张钊 或 @秦晓辉 添加权限"))
	}

	return username
}

func MaintainerRequired(r *http.Request) string {
	username := cookie.ReadUser(r)
	if username == "" {
		panic(errors.NotLoginError())
	}

	exists, err := model.UserExists(username)
	errors.MaybePanic(err)

	if !exists {
		errors.MaybePanic(fmt.Errorf("无权限，请联系管理员 @张钊 或 @秦晓辉 添加权限"))
	}

	if !config.IsMaintainer(username) {
		panic(errors.NoPrivError())
	}

	return username
}

// TaskMetaRequired 从URL中解析出task id，然后去查询task meta
func TaskMetaRequired(r *http.Request) *model.TaskMeta {
	idstr, found := mux.Vars(r)["id"]
	if !found {
		panic(errors.BadRequestError())
	}

	id, err := strconv.ParseInt(idstr, 10, 64)
	if err != nil {
		panic(errors.BadRequestError())
	}

	task, err := model.TaskMetaGet(id)
	if err != nil {
		panic(errors.InternalServerError(fmt.Sprintf("load task meta %d occur error %v", id, err)))
	}

	if task == nil {
		panic(errors.NotFoundError("no task found"))
	}

	return task
}

func TaskRequired(r *http.Request) (*model.TaskMeta, error) {
	idstr, found := mux.Vars(r)["id"]
	if !found {
		return nil, fmt.Errorf("id is necessary")
	}

	id, err := strconv.ParseInt(idstr, 10, 64)
	if err != nil {
		return nil, fmt.Errorf("id must int")
	}

	task, err := model.TaskMetaGet(id)
	if err != nil {
		return nil, fmt.Errorf(fmt.Sprintf("load task meta %d occur error %v", id, err))
	}

	if task == nil {
		return nil, fmt.Errorf("no task found")
	}

	return task, nil
}

func TplRequired(r *http.Request) *model.Tpl {
	idstr, found := mux.Vars(r)["id"]
	if !found {
		panic(errors.BadRequestError())
	}

	id, err := strconv.ParseInt(idstr, 10, 64)
	if err != nil {
		panic(errors.BadRequestError())
	}

	tpl, err := model.TplGet(id)
	if err != nil {
		panic(errors.InternalServerError(fmt.Sprintf("load tpl %d occur error %v", id, err)))
	}

	if tpl == nil {
		panic(errors.NotFoundError("no tpl found"))
	}

	return tpl
}

func APITplRequired(r *http.Request) (*model.Tpl, error) {
	idstr, found := mux.Vars(r)["id"]
	if !found {
		return nil, fmt.Errorf("not found id")
	}

	id, err := strconv.ParseInt(idstr, 10, 64)
	if err != nil {
		return nil, err
	}

	tpl, err := model.TplGet(id)
	if err != nil {
		return nil, err
	}

	if tpl == nil {
		return nil, fmt.Errorf("not fonud tpl[%d]", id)
	}

	return tpl, nil
}

func GrpRequired(r *http.Request) *model.Grp {
	idstr, found := mux.Vars(r)["gid"]
	if !found {
		panic(errors.BadRequestError())
	}

	id, err := strconv.ParseInt(idstr, 10, 64)
	if err != nil {
		panic(errors.BadRequestError())
	}

	obj, err := model.GrpGet(id)
	if err != nil {
		panic(errors.InternalServerError(fmt.Sprintf("load grp %d occur error %v", id, err)))
	}

	if obj == nil {
		panic(errors.NotFoundError("no grp found"))
	}

	return obj
}

func APIGrpRequired(r *http.Request) (*model.Grp, error) {
	idstr, found := mux.Vars(r)["gid"]
	if !found {
		return nil, fmt.Errorf("not found gid")
	}

	id, err := strconv.ParseInt(idstr, 10, 64)
	if err != nil {
		return nil, err
	}

	obj, err := model.GrpGet(id)
	if err != nil {
		return nil, err
	}

	if obj == nil {
		return nil, fmt.Errorf("not found gid[%d]", id)
	}

	return obj, nil
}

func GrpPrivRequired(r *http.Request) *model.Grp {
	g := GrpRequired(r)

	has, err := g.HasPriv(UsernameRequired(r))
	errors.MaybePanic(err)

	if !has {
		errors.Dangerous("no privilege")
	}

	return g
}

func APIGrpPrivRequired(r *http.Request) (*model.Grp, error) {
	g, err := APIGrpRequired(r)
	if err != nil {
		return nil, err
	}

	username, err := TokenRequired(r)
	if err != nil {
		return nil, err
	}

	has, err := g.HasPriv(username)
	if err != nil {
		return nil, err
	}

	if !has {
		return nil, fmt.Errorf("no privilege")
	}

	return g, nil
}

func StatusRequired(r *http.Request) string {
	status, found := mux.Vars(r)["status"]
	if !found {
		panic(errors.BadRequestError())
	}

	if status == "" {
		panic(errors.BadRequestError())
	}

	return status
}

func GrpMovePrivRequired(r *http.Request, id int64) {
	g, err := model.GrpGet(id)
	if err != nil {
		panic(errors.InternalServerError(fmt.Sprintf("load grp %d occur error %v", id, err)))
	}

	if g == nil {
		panic(errors.NotFoundError("no grp found"))
	}

	has, err := g.HasPriv(UsernameRequired(r))
	errors.MaybePanic(err)

	if !has {
		errors.Dangerous("no privilege")
	}
}

// 判断script中是否存在reboot和shutdown操作
func ScriptSafeRequired(script string) error {
	reg1, err := regexp.Compile(`\s+reboot$`)
	if err != nil {
		return err
	}

	reg2, err := regexp.Compile(`\s+reboot\s+`)
	if err != nil {
		return err
	}

	reg3, err := regexp.Compile(`\s+shutdown$`)
	if err != nil {
		return err
	}

	reg4, err := regexp.Compile(`\s+shutdown\s+`)
	if err != nil {
		return err
	}

	if reg1.MatchString(script) || reg2.MatchString(script) || reg3.MatchString(script) || reg4.MatchString(script) {
		return fmt.Errorf("reboot or shutdown is not supported")
	}

	return nil
}

func TokenRequired(r *http.Request) (string, error) {
	token, err := GetString(r, "token", "")
	if err != nil {
		return "", err
	}

	if strings.TrimSpace(token) == "" {
		return "", fmt.Errorf("token is blank")
	}

	username, err := model.FindToken(token)
	if err != nil {
		return "", nil
	}

	if username == "" {
		return "", fmt.Errorf("token not exist")
	}

	return username, nil
}

func GetString(r *http.Request, key string, defVal string) (string, error) {
	if val, ok := r.URL.Query()[key]; ok {
		if val[0] == "" {
			return defVal, nil
		}
		return strings.TrimSpace(val[0]), nil
	}

	if r.Form == nil {
		err := r.ParseForm()
		if err != nil {
			return "", nil
		}
	}

	val := r.Form.Get(key)
	if val == "" {
		return defVal, nil
	}

	return strings.TrimSpace(val), nil
}
