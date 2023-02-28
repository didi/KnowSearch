package model

type EdgeInfoInter struct {
	Dat   []EdgeAgentInfo `json:"dat"`
	Total int64           `json:"total"`
}

type EdgeAgentInfo struct {
	Ident string
	Ip    string
	Ts    int
}

func (EdgeAgentInfo) TableName() string {
	return "edge_agent_info"
}
