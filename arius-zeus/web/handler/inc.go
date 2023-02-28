package handler

import (
	"fmt"
	"net/http"

	"github.com/toolkits/str"
	"github.com/toolkits/web/errors"
)

func CheckDangerous(key, val string) {
	if str.HasDangerousCharacters(val) {
		errors.Panic("%s has dangerous characters", key)
	}
}

func location(r *http.Request) string {
	return fmt.Sprintf("http://%s%s", r.Host, r.RequestURI)
}
