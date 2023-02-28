package errors

import (
	"fmt"
	"net/http"
)

type Error struct {
	Code int    `json:"code"`
	Msg  string `json:"msg"`
}

func (this Error) Error() string {
	return fmt.Sprintf("%d:%s", this.Code, this.Msg)
}

// 400
func BadRequestError(msg ...string) Error {
	return _build(http.StatusBadRequest, "bad request", msg...)
}

// 401
func NotLoginError(msg ...string) Error {
	return _build(http.StatusUnauthorized, "unauthorized", msg...)
}

// 403
func NoPrivError(msg ...string) Error {
	return _build(http.StatusForbidden, "forbidden", msg...)
}

// 404
func NotFoundError(msg ...string) Error {
	return _build(http.StatusNotFound, "not found", msg...)
}

// 500
func InternalServerError(msg ...string) Error {
	return _build(http.StatusInternalServerError, "internal server error", msg...)
}

func _build(code int, defval string, custom ...string) Error {
	msg := defval
	if len(custom) > 0 {
		msg = custom[0]
	}
	return Error{
		Code: code,
		Msg:  msg,
	}
}

func MaybePanic(err error) {
	if err != nil {
		panic(Error{Msg: err.Error(), Code: http.StatusInternalServerError})
	}
}

func Dangerous(msg string) {
	if msg != "" {
		panic(Error{Msg: msg, Code: http.StatusInternalServerError})
	}
}

func Panic(msg string, args ...interface{}) {
	panic(Error{Msg: fmt.Sprintf(msg, args...), Code: http.StatusInternalServerError})
}
