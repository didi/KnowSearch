import fetch from '../lib/fetch';
const Prefix = "admin";
const POST = "POST";

type secondMetricsType = 'overview' | "node" | "index" | "app" | "dsl" | "clientNode";

// 获取gateway各模块的指标
export const getGatewayIndexList = (group: string) => {
  return fetch(`/v3/op/gateway/metrics/config/${group}`);
}

// 获取账号下已配置指标列表
export const getCheckedList = (secondMetricsType: secondMetricsType) => {
  return fetch("/v3/op/phy/cluster/metrics/configMetrics", {
    prefix: Prefix,
    method: POST,
    body: {
      domainAccount: "",
      firstMetricsType: "gateway",
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
      firstMetricsType: "gateway",
      secondMetricsType: secondMetricsType,
      metricsTypes: metricsTypes
    }
  });
}


//  获取总览视图数据
export const getOverviewData = (metricsTypes: string[], startTime: number, endTime: number) => {
  return fetch("/v3/op/gateway/metrics/overview", {
    prefix: Prefix,
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
    }
  });
}

// 获取节点视图
export const getNodeViewData = (metricsTypes: string[], startTime: number, endTime: number, topNu: number, nodeIp: string[]) => {
  return fetch("/v3/op/gateway/metrics/nodes", {
    prefix: Prefix,
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      nodeIps: nodeIp,
      topNu
    }
  });
}

// 获取clientNode节点视图
export const getClientNodeViewData = (metricsTypes: string[], startTime: number, endTime: number, topNu: number, nodeIp: string, clientNodeIp: string) => {
  return fetch("/v3/op/gateway/metrics/node/client", {
    prefix: Prefix,
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      nodeIp,
      topNu,
      clientNodeIp,
    }
  });
}

export const getNodeIpList = () => {
  return fetch("/v2/thirdpart/gateway/aliveNodeName");
}

//获取取gatewayNode相关的clientNode ip列表
export const getClientNodeList = (gatewayNode, startTime, endTime) => {
  return fetch(`/v3/op/gateway/metrics/node/client/list?gatewayNode=${gatewayNode || ''}&startTime=${startTime}&endTime=${endTime}`);
}

// 获取索引视图数据
export const getIndexViewData = (metricsTypes: string[], startTime: number, endTime: number, topNu: number, indexName: string) => {
  return fetch("/v3/op/gateway/metrics/index", {
    prefix: Prefix,
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      indexName,
      topNu
    }
  });
}


// 获取index视图 index 列表数据
export const getIndexNameList = () => {
  return fetch("/v3/op/template/logic/listNames");
}

// 获取项目视图数据
export const getProjectViewData = (metricsTypes: string[], startTime: number, endTime: number, topNu: number, appId: string) => {
  return fetch("/v3/op/gateway/metrics/app", {
    prefix: Prefix,
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      appId,
      topNu
    }
  });
}

export const getAppIdList = () => {
  return fetch("/v3/op/app/list");
}

// 获取查询模板数据
export const getQueryTemplateData = (metricsTypes: string[], startTime: number, endTime: number, topNu: number, dslMd5: string) => {
  return fetch("/v3/op/gateway/metrics/dsl", {
    prefix: Prefix,
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      dslMd5,
      topNu
    }
  });
}

export const getDslMd5List = (startTime: number, endTime: number) => {
  return fetch(`/v3/op/gateway/metrics/dslMd5/list?startTime=${startTime}&endTime=${endTime}`);
}