package config

import (
	"fmt"
	"io/ioutil"
	"log"

	"github.com/toolkits/file"
	"gopkg.in/yaml.v2"
)

type ConfYaml struct {
	Debug         bool         `yaml:"debug"`
	Listen        string       `yaml:"listen"`
	SpNoFinStatus []string     `yaml:"spNoFinStatus"`
	Table         int64        `yaml:"table"`
	MySQL         SectionMySQL `yaml:"mysql"`
}

type SectionMySQL struct {
	Addr    string `yaml:"addr"`
	Idle    int    `yaml:"idle"`
	Max     int    `yaml:"max"`
	ShowSQL bool   `yaml:"showsql"`
}

var G *ConfYaml

func Parse(cfg string) error {
	if cfg == "" {
		return fmt.Errorf("use -c to specify configuratio file")
	}

	if !file.IsExist(cfg) {
		return fmt.Errorf("configuration file %s is nonexistent", cfg)
	}

	bs, err := ioutil.ReadFile(cfg)
	if err != nil {
		return fmt.Errorf("read configuration file %s fail %s", cfg, err.Error())
	}

	var c ConfYaml
	err = yaml.Unmarshal(bs, &c)
	if err != nil {
		return fmt.Errorf("parse configuration file %s fail %s", cfg, err.Error())
	}

	G = &c
	log.Println("[I] load configuration file", cfg, "successfully")
	return nil
}
