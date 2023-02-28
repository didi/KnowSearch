package rpc

import (
	"fmt"

	"zeus/common/model"
	"zeus/exec/backend"
)

func (*Exec) GetTaskMeta(id int64, resp *model.TaskMetaResponse) error {
	meta, err := backend.GetTaskMeta(id)
	if err != nil {
		resp.Message = err.Error()
		return nil
	}

	if meta == nil {
		resp.Message = fmt.Sprintf("task %d not found", id)
		return nil
	}

	resp.Script = meta.Script
	resp.Args = meta.Args
	resp.Account = meta.Account
	return nil
}
