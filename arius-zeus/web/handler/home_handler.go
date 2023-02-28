package handler

import (
	"net/http"

	"github.com/toolkits/web/errors"
	"github.com/toolkits/web/param"

	"zeus/web/config"
	"zeus/web/http/cookie"
	"zeus/web/http/render"
	"zeus/web/model"
)

func HomeIndex(w http.ResponseWriter, r *http.Request) {
	callback := param.String(r, "callback", "/grps")
	username := cookie.ReadUser(r)
	if username != "" {
		http.Redirect(w, r, callback, 302)
		return
	}

	render.Put(r, "Callback", callback)
	render.HTML(r, w, "home/login")
}

func Logout(w http.ResponseWriter, r *http.Request) {
	cookie.RemoveUser(w)
	http.Redirect(w, r, "/", 302)
}

func Login(w http.ResponseWriter, r *http.Request) {
	username := param.MustString(r, "username")
	password := param.MustString(r, "password")

	user, err := model.UserGet(username)
	errors.MaybePanic(err)

	if user == nil {
		errors.Dangerous("no such user")
	}

	if config.CryptoPass(password) != user.Password {
		errors.Dangerous("password error")
	}

	render.Message(w, cookie.WriteUser(w, username))
}
