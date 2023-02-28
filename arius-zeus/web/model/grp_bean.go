package model

import (
	"errors"
	"strings"
)

type Grp struct {
	ID    int64  `gorm:"primary_key" json:"id"`
	PID   int64  `gorm:"column:pid" json:"pid"`
	Name  string `json:"name"`
	Users string `json:"users"`
}

func (Grp) TableName() string {
	return "grp"
}

func (g *Grp) HasPriv(username string) (bool, error) {
	arr := strings.Fields(g.Users)
	cnt := len(arr)
	for i := 0; i < cnt; i++ {
		if arr[i] == username {
			return true, nil
		}
	}

	if g.PID == 0 {
		return false, nil
	}

	pGrp, err := GrpGet(g.PID)
	if err != nil {
		return false, err
	}

	return pGrp.HasPriv(username)
}

func (g *Grp) Update(pid int64, name, users string) error {
	return DB.Exec("UPDATE grp SET pid=?, name=?, users=? WHERE id=?", pid, name, users, g.ID).Error
}

func (g *Grp) Del() error {
	cnt, err := TplCount(g.ID)
	if err != nil {
		return err
	}

	if cnt > 0 {
		return errors.New("there are still tpls under " + g.Name)
	}

	cnt, err = GrpCount(g.ID)
	if err != nil {
		return err
	}

	if cnt > 0 {
		return errors.New("there are still grps under " + g.Name)
	}

	return DB.Exec("DELETE FROM grp WHERE id=?", g.ID).Error
}

func (g *Grp) ParentGrps() []*Grp {
	var gs []*Grp
	if g.PID == 0 {
		return gs
	}

	var err error

	tmp := g
	for {
		tmp, err = GrpGet(tmp.PID)
		if err != nil {
			return []*Grp{}
		}

		gs = append(gs, tmp)
		if tmp.PID == 0 {
			break
		}
	}

	cnt := len(gs)
	ret := make([]*Grp, cnt)
	for i := 0; i < cnt; i++ {
		ret[i] = gs[cnt-1-i]
	}

	return ret
}
