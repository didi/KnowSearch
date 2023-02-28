package render

import (
	"html/template"
	"net/http"
	"strings"

	"github.com/gorilla/context"
	"github.com/toolkits/time"
	"github.com/unrolled/render"

	"zeus/web/config"
	"zeus/web/http/cookie"
	"zeus/web/http/helper"
)

var Render *render.Render
var funcMap = template.FuncMap{
	"HumanDuration": time.HumanDurationInt,
	"TimeFormat":    time.FormatTsInt,
	"StatusClass":   helper.StatusClass,
}

func Init() {
	Render = render.New(render.Options{
		Directory:     "views",
		Extensions:    []string{".html"},
		Delims:        render.Delims{"{{", "}}"},
		Funcs:         []template.FuncMap{funcMap},
		IndentJSON:    false,
		IsDevelopment: config.G.Debug,
	})
}

func Put(r *http.Request, key string, val interface{}) {
	m, ok := context.GetOk(r, "_DATA_MAP_")
	if ok {
		mm := m.(map[string]interface{})
		mm[key] = val
		context.Set(r, "_DATA_MAP_", mm)
	} else {
		context.Set(r, "_DATA_MAP_", map[string]interface{}{key: val})
	}
}

func HTML(r *http.Request, w http.ResponseWriter, name string, htmlOpt ...render.HTMLOptions) {
	Put(r, "Version", config.Version)

	loginUser := cookie.ReadUser(r)
	Put(r, "LoginUser", strings.ToLower(loginUser))

	isMaintainer := false
	if loginUser != "" {
		for i := 0; i < len(config.G.Maintainers); i++ {
			if loginUser == config.G.Maintainers[i] {
				isMaintainer = true
				break
			}
		}
	}

	Put(r, "IsMaintainer", isMaintainer)
	Render.HTML(w, http.StatusOK, name, context.Get(r, "_DATA_MAP_"), htmlOpt...)
}

func Text(w http.ResponseWriter, v string, codes ...int) {
	code := http.StatusOK
	if len(codes) > 0 {
		code = codes[0]
	}
	Render.Text(w, code, v)
}

func Message(w http.ResponseWriter, err error) {
	msg := ""
	if err != nil {
		msg = err.Error()
	}

	Render.JSON(w, http.StatusOK, map[string]string{"msg": msg})
}

func Data(w http.ResponseWriter, v interface{}, err error) {
	if err != nil {
		Render.JSON(w, http.StatusOK, map[string]string{"msg": err.Error()})
	} else {
		Render.JSON(w, http.StatusOK, map[string]interface{}{"msg": "", "data": v})
	}
}
