import fetch from "../lib/fetch";
const POST = "POST";
const v3Prefix = `/v3`;
const dashboardMetrics = `/dashboard/metrics`;

export interface ILineParams {
  metricsTypes: string[];
  startTime: number;
  endTime: number;
  topNu: number;
  aggType: string;
}

// /v3/dashboard/metrics/health 获取dashboard大盘健康状态信息
export const dashboardHealth = () => {
  return fetch(`${v3Prefix}${dashboardMetrics}/health`);
};

// /v3/dashboard/metrics/list/cluster 获取dashboard大盘集群相关list列表指标信息
export const clusterList = (params: { aggType: string; metricsTypes: string[] }) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/list/cluster`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/list/index 获取dashboard大盘索引相关list列表指标信息
export const indexList = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/list/index`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/list/node 获取dashboard大盘节点相关list列表指标信息
export const nodeList = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/list/node`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/list/template 获取dashboard大盘模板相关list列表指标信息
export const templatelist = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/list/template`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/top/cluster 获取dashboard大盘TopN集群相关指标信息
export const clusterLine = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/top/cluster`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/top/cluster-thread-pool-queue 获取dashboard大盘TopNES线程池相关指标信息
export const clusterThreadPoolQueue = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/top/cluster-thread-pool-queue`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/top/index 获取dashboard大盘TopN索引相关指标信息
export const indexLine = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/top/index`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/top/node 获取dashboard大盘TopN节点相关指标信息
export const nodeLine = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/top/node`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/dashboard/metrics/top/template 获取dashboard大盘TopN模板相关指标信息
export const templateLine = (params: ILineParams) => {
  return fetch(`${v3Prefix}${dashboardMetrics}/top/template`, {
    method: POST,
    body: JSON.stringify(params),
  });
};

// /v3/metrics/cluster/config-metrics 获取账号下已配置指标类型
export const getCheckedList = (secondUserConfigType: string) => {
  return fetch(`${v3Prefix}/metrics/cluster/config-metrics`, {
    method: POST,
    body: {
      userName: "",
      firstUserConfigType: "dashboard",
      secondUserConfigType,
      userConfigTypes: [],
    },
  });
};

// /v3/metrics/cluster/config-metrics 更新账号下已配置指标类型
export const setCheckedList = (secondUserConfigType: string, userConfigTypes: string[]) => {
  return fetch(`${v3Prefix}/metrics/cluster/config-metrics`, {
    method: "PUT",
    body: {
      userName: "",
      firstUserConfigType: "dashboard",
      secondUserConfigType,
      userConfigTypes,
    },
  });
};

// /v3/dashboard/metrics/health 获取dashboard大盘健康状态信息
export const dashboardIndex = () => {
  return fetch(`/v3/dashboard/metrics/dashboard-threshold`);
};

// 通过model筛选获取指标字典信息
export const getDictionary = (params) => {
  return fetch(`${v3Prefix}/metrics/dictionary`, {
    method: "POST",
    body: params,
  });
};
