package handler

import (
	"net/http"
	"time"

	"github.com/toolkits/web/errors"

	"fmt"
	"zeus/web/http/render"
	"zeus/web/model"
	"zeus/web/utils"
)

func TokenList(w http.ResponseWriter, r *http.Request) {
	username := UsernameRequired(r)

	token, err := model.GetTokenByUsername(username)
	errors.MaybePanic(err)

	render.Put(r, "Token", token)
	render.HTML(r, w, "token/index")
}

func SetToken(w http.ResponseWriter, r *http.Request) {
	username := UsernameRequired(r)

	_, err := setToken(username)
	errors.MaybePanic(err)

	http.Redirect(w, r, "/token/list", 302)
	return
}

func setToken(username string) (string, error) {
	for i := 0; i < 50; i++ {
		token := utils.GenerateToken(username)
		find, err := model.FindToken(token)
		if err != nil {
			return "", err
		}

		if find == "" {
			err := model.InsertToken(username, token)
			if err != nil {
				return "", err
			}

			return token, nil
		}
		time.Sleep(time.Second)
	}

	return "", fmt.Errorf("生成token失败，请重试")
}
