import fetch from '../lib/fetch';
const Prefix = "admin";
const POST = "POST";

// 获取项目下的物理集群名称列表
export const getClusterNameList = () => {
  return fetch("/v3/op/phy/cluster/names", {
    prefix: Prefix
  });
}

type secondMetricsType = 'overview' | "node" | "index";
type aggType = "max" | "avg";

// 获取账号下已配置指标列表
export const getCheckedList = (secondMetricsType: secondMetricsType) => {
  return fetch("/v3/op/phy/cluster/metrics/configMetrics", {
    prefix: Prefix,
    method: POST,
    body: {
      domainAccount: "",
      firstMetricsType: "cluster",
      secondMetricsType: secondMetricsType,
      metricsTypes: []
    }
  });
}

// 设置账号下已配置指标列表
export const setCheckedList = (secondMetricsType: secondMetricsType, metricsTypes: string[]) => {
  return fetch("/v3/op/phy/cluster/metrics/updateConfigMetrics", {
    prefix: Prefix,
    method: POST,
    body: {
      domainAccount: "",
      firstMetricsType: "cluster",
      secondMetricsType: secondMetricsType,
      metricsTypes: metricsTypes
    }
  });
}

// 获取总览视图数据
export const getOverviewData = (metricsTypes: string[], clusterPhyName: string, startTime: number, endTime: number) => {
  if (!clusterPhyName) {
    return;
  }
  return fetch("/v3/op/phy/cluster/metrics/overview", {
    prefix: Prefix,
    method: POST,
    body: {
      "aggType": "max",
      metricsTypes,
      clusterPhyName,
      startTime,
      endTime,
    }
  });
}

// 获取节点视图数据
export const getNodeViewData = (metricsTypes: string[], clusterPhyName: string, startTime: number, endTime: number, topNu: number, nodeIp: string) => {
  if (!clusterPhyName) {
    return;
  }
  return fetch("/v3/op/phy/cluster/metrics/node", {
    prefix: Prefix,
    method: POST,
    body: {
      "aggType": "avg",
      metricsTypes,
      clusterPhyName,
      startTime,
      endTime,
      topNu,
      nodeName: nodeIp
    }
  });
}

// 获取节点视图数据 Ip 名称列表
export const getNodeIpList = (clusterPhyName: string) => {
  return fetch(`/v3/op/phy/cluster/${clusterPhyName}/nodes`, { prefix: Prefix });
}


// 获取index视图数据
export const getIndexViewData = (metricsTypes: string[], clusterPhyName: string, startTime: number, endTime: number, topNu: number, indexName: string) => {
  if (!clusterPhyName) {
    return;
  }
  return fetch("/v3/op/phy/cluster/metrics/index", {
    prefix: Prefix,
    method: POST,
    body: {
      "aggType": "avg",
      metricsTypes,
      clusterPhyName,
      startTime,
      endTime,
      topNu,
      indexName
    }
  });
}

// 获取index视图 index 列表数据
export const getIndexNameList = (clusterPhyName) => {
  return fetch(`/v3/op/phy/cluster/metrics/${clusterPhyName}/indices`, { prefix: Prefix });
}