package main

import (
	"flag"
	"fmt"
	"log"
	"os"

	_ "github.com/jinzhu/gorm/dialects/mysql"

	"zeus/scheduler/backend"
	"zeus/scheduler/config"
	"zeus/scheduler/rpc"
	"zeus/scheduler/timer"
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
	backend.InitCache()
	backend.InitMySQL()

	go timer.Heartbeat()
	go timer.Schedule()
	go timer.CheckTimeout()

	rpc.Start()
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
