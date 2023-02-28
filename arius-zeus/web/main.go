package main

import (
	"flag"
	"fmt"
	"log"
	"os"

	_ "github.com/jinzhu/gorm/dialects/mysql"

	"zeus/web/config"
	"zeus/web/http"
	"zeus/web/model"
)

func init() {
	log.SetFlags(log.Ldate | log.Ltime | log.Lshortfile)

	cfg := flag.String("c", "cfg.yml", "configuration file")
	version := flag.Bool("v", false, "show version")
	help := flag.Bool("h", false, "help")
	flag.Parse()

	handleVersion(*version)
	handleHelp(*help)
	handleConfig(*cfg)
}

func main() {
	model.InitMySQL()
	model.InitCache()
	model.InitRoot()
	model.StartCleaner()
	http.Start()
}

func handleVersion(displayVersion bool) {
	if displayVersion {
		fmt.Println(config.Version)
		os.Exit(0)
	}
}

func handleHelp(displayHelp bool) {
	if displayHelp {
		flag.Usage()
		os.Exit(0)
	}
}

func handleConfig(configFile string) {
	err := config.Parse(configFile)
	if err != nil {
		log.Fatalln(err)
	}
}
