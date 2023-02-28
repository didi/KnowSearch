package config

import (
	"github.com/toolkits/str"
)

func IsMaintainer(user string) bool {
	count := len(G.Maintainers)
	for i := 0; i < count; i++ {
		if user == G.Maintainers[i] {
			return true
		}
	}

	return false
}

func CheckAuth(user, pass string) bool {
	count := len(G.Auths)
	for i := 0; i < count; i++ {
		if G.Auths[i].Name == user && G.Auths[i].Pass == pass {
			return true
		}
	}
	return false
}

func CryptoPass(raw string) string {
	return str.Md5Encode("<-*uK35^263Y*->" + raw)
}
