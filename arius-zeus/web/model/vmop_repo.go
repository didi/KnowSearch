package model

var VmopRepo = new(Vmop)

func (v *Vmop) Insert(hostnames []string, usernames []string) error {
	hostslen := len(hostnames)
	userslen := len(usernames)

	for i := 0; i < hostslen; i++ {
		for j := 0; j < userslen; j++ {
			exists, err := v.Exists(hostnames[i], usernames[j])
			if err != nil {
				return err
			}
			if exists {
				continue
			}

			bean := new(Vmop)
			bean.Hostname = hostnames[i]
			bean.Username = usernames[j]
			err = DB.Create(bean).Error
			if err != nil {
				return err
			}
		}
	}

	return nil
}

func (v *Vmop) NopriHosts(hostnames []string, username string) ([]string, error) {
	count := len(hostnames)
	nopri := make([]string, 0, count)
	for i := 0; i < count; i++ {
		if hostnames[i] == "" {
			continue
		}

		exists, err := v.Exists(hostnames[i], username)
		if err != nil {
			return nil, err
		}

		if !exists {
			nopri = append(nopri, hostnames[i])
		}
	}
	return nopri, nil
}

func (v *Vmop) Exists(hostname, username string) (bool, error) {
	var count int
	err := DB.Table("vmop").Where("hostname=? and username=?", hostname, username).Count(&count).Error
	return count > 0, err
}
