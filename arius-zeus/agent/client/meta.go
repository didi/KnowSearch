package client

import (
	"fmt"
	"log"

	"zeus/common/model"
)

// Meta 根据ID去Exec获取任务元信息
func Meta(id int64) (script string, args string, account string, err error) {
	var resp model.TaskMetaResponse
	err = GetExecClient().Call("Exec.GetTaskMeta", id, &resp, 0)
	if err != nil {
		return
	}

	if resp.Message != "" {
		log.Println("[E] rpc call Exec.GetTaskMeta get error message: ", resp.Message)
		err = fmt.Errorf(resp.Message)
		return
	}

	script = resp.Script
	args = resp.Args
	account = resp.Account
	return
}
