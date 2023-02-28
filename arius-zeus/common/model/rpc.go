package model

// 任务执行完成之后的结果
type ReportTask struct {
	ID     int64  // 任务的ID
	TS     int    // 记录任务下发的时间
	Status string // 状态码
	Stdout string // 任务的标准输出
	Stderr string // 任务的标准错误输出
}

// agent向exec发起的心跳请求
type ReportRequest struct {
	Hostname    string
	ReportTasks []ReportTask
	Baseinfo    map[string]string
}

// ReportResponse Exec向Agent返回，这个Agent还有哪些任务要执行
type ReportResponse struct {
	Message     string
	AssignTasks []AssignTask
}

// AssignTask Exec下发给某Agent的任务
type AssignTask struct {
	ID     int64
	TS     int
	Action string
}

// exec从scheduler获取TaskMeta
type TaskMetaResponse struct {
	Message string
	Script  string
	Args    string
	Account string
}
