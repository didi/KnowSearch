package http

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	"zeus/agent/config"
)

func init() {
	http.HandleFunc("/version", func(w http.ResponseWriter, r *http.Request) {
		w.Write([]byte(config.Version))
	})

	http.HandleFunc("/pid", func(w http.ResponseWriter, r *http.Request) {
		w.Write([]byte(fmt.Sprint(os.Getpid())))
	})

	http.HandleFunc("/exit", func(w http.ResponseWriter, r *http.Request) {
		if !strings.HasPrefix(r.RemoteAddr, "127.0.0.1") {
			w.Write([]byte("no privilege"))
			return
		}

		w.Write([]byte("exiting..."))
		go func() {
			time.Sleep(time.Millisecond * 100)
			os.Exit(0)
		}()
	})
}

func Start() {
	s := &http.Server{
		Addr:           config.G.Listen,
		MaxHeaderBytes: 1 << 30,
	}

	log.Println("listening on:", config.G.Listen)
	log.Fatalln(s.ListenAndServe())
}
