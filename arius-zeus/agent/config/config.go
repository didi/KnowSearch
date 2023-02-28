package config

import (
	"fmt"
	"io/ioutil"
	"log"
	"strings"

	"github.com/toolkits/file"
	"github.com/toolkits/sys"
	"gopkg.in/yaml.v2"
)

type ConfYaml struct {
	Hostname string            `yaml:"hostname"`
	Listen   string            `yaml:"listen"`
	Metadir  string            `yaml:"metadir"`
	Interval int64             `yaml:"interval"`
	Servers  []string          `yaml:"servers"`
	Baseinfo map[string]string `yaml:"baseinfo"`
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

	c.Metadir = strings.TrimSpace(c.Metadir)

	if len(c.Metadir) == 0 {
		c.Metadir = "./meta"
	}

	c.Metadir, err = file.RealPath(c.Metadir)
	if err != nil {
		return fmt.Errorf("get absolute filepath of %s fail %v", c.Metadir, err)
	}

	if err = file.InsureDir(c.Metadir); err != nil {
		return fmt.Errorf("mkdir -p %s fail: %v", c.Metadir, err)
	}

	if c.Listen == "" {
		return fmt.Errorf("configuration item[listen] is blank")
	}

	G = &c
	log.Printf("[I] load configuration file %s successfully", cfg)
	log.Printf("[I] %+v", c)
	return nil
}

func GetBase() map[string]string {
	baseCmd := G.Baseinfo
	result := make(map[string]string, len(baseCmd))
	for k, cmd := range baseCmd {
		v, err := sys.CmdOutNoLn("/bin/bash", "-c", cmd)
		if err != nil {
			result[k] = err.Error()
		}

		result[k] = v
	}

	return result
}
