package utils

import (
	"fmt"
	"regexp"
	"strings"
)

// CleanIps 对用户输入的IP做安全校验，返回合法的数组
func CleanIps(ips string) ([]string, error) {
	iparr := strings.Fields(ips)
	if len(iparr) == 0 {
		return nil, fmt.Errorf("no ips given")
	}

	re, err := regexp.Compile(`^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$`)
	if err != nil {
		return nil, err
	}

	// 用来去重
	set := make(map[string]struct{})

	iplen := len(iparr)
	ret := make([]string, 0, iplen)

	for i := 0; i < iplen; i++ {
		if !re.MatchString(iparr[i]) {
			continue
			//return nil, fmt.Errorf("[%s] not ip pattern", iparr[i])
		}

		if _, found := set[iparr[i]]; found {
			continue
		}

		set[iparr[i]] = struct{}{}
		ret = append(ret, iparr[i])
	}

	return ret, nil
}
