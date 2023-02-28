package http

import (
	"log"
	"time"

	"github.com/codegangsta/negroni"
	"github.com/gorilla/mux"
	"github.com/toolkits/file"
	"gopkg.in/tylerb/graceful.v1"

	"zeus/web/config"
	"zeus/web/http/cookie"
	"zeus/web/http/middleware"
	"zeus/web/http/render"
)

func Start() {
	render.Init()
	cookie.Init()

	r := mux.NewRouter().StrictSlash(false)
	ConfigRoutes(r)

	n := negroni.New()
	n.Use(middleware.NewRecovery(file.MustOpenLogFile(config.G.Log.Error)))
	n.Use(middleware.NewLogger(file.MustOpenLogFile(config.G.Log.Access)))

	n.UseHandler(r)

	log.Println("listening http on", config.G.HTTP.Listen)
	graceful.Run(config.G.HTTP.Listen, time.Duration(10)*time.Second, n)
}
