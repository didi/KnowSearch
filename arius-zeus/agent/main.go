package main

import (
	"flag"
	"fmt"
	"log"
	"os"

	"zeus/agent/config"
	"zeus/agent/http"
	"zeus/agent/timer"
)

func init() {
	log.SetFlags(log.Ldate | log.Ltime | log.Lshortfile)

	cfg := flag.String("c", "cfg.yml", "configuration file")
	version := flag.Bool("v", false, "show version")
	help := flag.Bool("h", false, "help")
	runv := flag.String("r", config.Version, "for odin super agent")
	flag.Parse()

	*runv = config.Version

	handleVersion(*version)
	handleHelp(*help)
	handleConfig(*cfg)
}

func main() {
	go http.Start()
	timer.Heartbeat()
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
