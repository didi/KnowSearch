package cookie

import (
	"net/http"
	"strings"

	"github.com/gorilla/securecookie"

	"zeus/web/config"
)

const USER_COOKIE_NAME = "zeus"

var SecureCookie *securecookie.SecureCookie

func Init() {
	var hashKey = []byte(config.G.HTTP.Secret)
	var blockKey = []byte("36005025221c6e1ff4bf9b255e49d999")
	SecureCookie = securecookie.New(hashKey, blockKey)
}

type CookieData struct {
	Username string
}

func ReadUser(r *http.Request) string {
	if cookie, err := r.Cookie(USER_COOKIE_NAME); err == nil {
		var value CookieData
		if err = SecureCookie.Decode(USER_COOKIE_NAME, cookie.Value, &value); err == nil {
			return value.Username
		}
	}

	return ""
}

func ReadBrokerCookie(r *http.Request) string {
	if cookie, err := r.Cookie("bc"); err == nil {
		return cookie.Value
	}

	return ""
}

func WriteUser(w http.ResponseWriter, username string) error {
	username = strings.ToLower(username)
	value := CookieData{Username: username}
	encoded, err := SecureCookie.Encode(USER_COOKIE_NAME, value)
	if err != nil {
		return err
	}

	cookie := &http.Cookie{
		Name:     USER_COOKIE_NAME,
		Value:    encoded,
		Path:     "/",
		MaxAge:   3600 * 24 * 7,
		HttpOnly: true,
	}
	http.SetCookie(w, cookie)
	return nil
}

func WriteBrokerCookie(w http.ResponseWriter, bc string) error {
	cookie := &http.Cookie{
		Name:     "bc",
		Value:    bc,
		Path:     "/",
		MaxAge:   3600 * 24 * 7,
		HttpOnly: true,
	}
	http.SetCookie(w, cookie)
	return nil
}

func RemoveUser(w http.ResponseWriter) error {
	var value CookieData
	encoded, err := SecureCookie.Encode(USER_COOKIE_NAME, value)
	if err != nil {
		return err
	}

	cookie := &http.Cookie{
		Name:     USER_COOKIE_NAME,
		Value:    encoded,
		Path:     "/",
		MaxAge:   -1,
		HttpOnly: true,
	}
	http.SetCookie(w, cookie)
	return nil
}

func RemoveBrokerCookie(w http.ResponseWriter) error {
	cookie := &http.Cookie{
		Name:     "bc",
		Value:    "encoded",
		Path:     "/",
		MaxAge:   -1,
		HttpOnly: true,
	}
	http.SetCookie(w, cookie)
	return nil
}
