import fetch from '../lib/fetch';
const POST = "POST";

export interface ILineParams {
  metricsTypes: string[], 
  startTime: number, 
  endTime: number,
  topNu: number,
  aggType: string,
}

// /v3/op/dashboard/metrics/health 获取dashboard大盘健康状态信息
export const dashboardHealth = () => {
  return fetch(`/v3/op/dashboard/metrics/health`)
};

//  /v3/op/dashboard/metrics/list/cluster 获取dashboard大盘list列表指标信息
export const clusterList = (params : {
  aggType: string,
  metricsTypes: string[]
}) => {
  return fetch(`/v3/op/dashboard/metrics/list/cluster`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// /v3/op/dashboard/metrics/top/cluster 获取dashboard大盘TopN集群相关指标信息
export const clusterLine = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/top/cluster`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// /v3/op/dashboard/metrics/list/index 获取dashboard大盘索引相关list列表指标信息
export const indexList = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/list/index`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// POST /v3/op/dashboard/metrics/top/index 获取dashboard大盘TopN索引相关指标信息
export const indexLine = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/top/index`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// /v3/op/dashboard/metrics/list/node 获取dashboard大盘节点相关list列表指标信息
export const nodeList = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/list/node`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// POST /v3/op/dashboard/metrics/top/node 获取dashboard大盘TopN节点相关指标信息
export const nodeLine = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/top/node`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// POST/v3/op/dashboard/metrics/list/template 获取dashboard大盘模板相关list列表指标信息
export const templatelist = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/list/template`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// POST /v3/op/dashboard/metrics/top/node 获取dashboard大盘TopN节点相关指标信息
export const templateLine = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/top/template`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// POST /v3/op/dashboard/metrics/top/clusterThreadPoolQueue 获取dashboard大盘TopNES线程池相关指标信息
export const clusterThreadPoolQueue = (params : ILineParams) => {
  return fetch(`/v3/op/dashboard/metrics/top/clusterThreadPoolQueue`, {
    method: POST,
    body: JSON.stringify(params)
  });
};

// 获取账号下已配置指标列表
export const getCheckedList = (secondMetricsType: string) => {
  return fetch("/v3/op/phy/cluster/metrics/configMetrics", {
    method: POST,
    body: {
      domainAccount: "",
      firstMetricsType: "dashboard",
      secondMetricsType: secondMetricsType,
      metricsTypes: []
    }
  });
}

// 设置账号下已配置指标列表
export const setCheckedList = (secondMetricsType: string, metricsTypes: string[]) => {
  return fetch("/v3/op/phy/cluster/metrics/updateConfigMetrics", {
    method: POST,
    body: {
      domainAccount: "",
      firstMetricsType: "dashboard",
      secondMetricsType: secondMetricsType,
      metricsTypes: metricsTypes
    }
  });
}