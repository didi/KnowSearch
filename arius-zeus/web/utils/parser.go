package utils

import (
	"strings"
)

func ParseLines(lines string) []string {
	lines = strings.Replace(lines, "\n\r", "\n", -1)
	lines = strings.Replace(lines, "\r", "\n", -1)

	rawArr := strings.Fields(lines)
	rawLen := len(rawArr)

	newArr := make([]string, 0, rawLen)
	set := make(map[string]struct{}, rawLen)

	for i := 0; i < rawLen; i++ {
		if _, found := set[rawArr[i]]; found {
			continue
		}

		set[rawArr[i]] = struct{}{}
		newArr = append(newArr, rawArr[i])
	}

	return newArr
}

func MtoL(m map[string]struct{}) []string {
	cnt := len(m)
	lst := make([]string, 0, cnt)
	for v := range m {
		lst = append(lst, v)
	}
	return lst
}
