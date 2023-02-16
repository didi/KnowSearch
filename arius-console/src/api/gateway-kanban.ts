import fetch from "../lib/fetch";
const prefix = "/v3";
const POST = "POST";

type secondUserConfigType = "overview" | "node" | "index" | "app" | "dsl" | "clientNode";

// 获取账号下已配置指标列表
export const getCheckedList = (secondUserConfigType: secondUserConfigType) => {
  return fetch(`${prefix}/metrics/cluster/config-metrics`, {
    method: "POST",
    body: {
      userName: "",
      firstUserConfigType: "gateway",
      secondUserConfigType,
      userConfigTypes: [],
    },
  });
};

// 设置账号下已配置指标列表
export const setCheckedList = (secondUserConfigType: secondUserConfigType, userConfigTypes: string[]) => {
  return fetch(`${prefix}/metrics/cluster/config-metrics`, {
    method: "PUT",
    body: {
      userName: "",
      firstUserConfigType: "gateway",
      secondUserConfigType,
      userConfigTypes,
    },
  });
};

//  获取总览视图数据
export const getOverviewData = (metricsTypes: string[], startTime: number, endTime: number) => {
  return fetch(`${prefix}/metrics/gateway/overview`, {
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
    },
  });
};

// 获取节点视图
export const getNodeViewData = (
  metricsTypes: string[],
  startTime: number,
  endTime: number,
  topNu: number,
  nodeIp: string[],
  topMethod: string,
  topTimeStep: number
) => {
  let body = {
    metricsTypes,
    startTime,
    endTime,
    nodeIps: nodeIp,
    topNu,
    topMethod,
    topTimeStep,
  };
  return fetch(`${prefix}/metrics/gateway/nodes`, {
    method: POST,
    body,
  });
};

// 获取clientNode节点视图
export const getClientNodeViewData = (
  metricsTypes: string[],
  startTime: number,
  endTime: number,
  topNu: number,
  nodeIp: string,
  clientNodeIp: string,
  topMethod: string,
  topTimeStep: number
) => {
  return fetch(`${prefix}/metrics/gateway/client-node`, {
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      nodeIp,
      topNu,
      clientNodeIp,
      topMethod,
      topTimeStep,
    },
  });
};

export const getNodeIpList = () => {
  return fetch(`${prefix}/metrics/gateway/alive-nodes`);
};

//获取取gatewayNode相关的clientNode ip列表
export const getClientNodeList = (gatewayNode, startTime, endTime) => {
  return fetch(`${prefix}/metrics/gateway/client-node-ip?gatewayNode=${gatewayNode || ""}&startTime=${startTime}&endTime=${endTime}`);
};

// 获取索引视图数据
export const getIndexViewData = (
  metricsTypes: string[],
  startTime: number,
  endTime: number,
  topNu: number,
  indexName: string,
  topMethod: string,
  topTimeStep: number
) => {
  return fetch(`${prefix}/metrics/gateway/index`, {
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      indexName,
      topNu,
      topMethod,
      topTimeStep,
    },
  });
};

// 获取index视图 index 列表数据
export const getIndexNameList = () => {
  return fetch(`${prefix}/template/logic/names`);
};

// 获取项目视图数据
export const getProjectViewData = (
  metricsTypes: string[],
  startTime: number,
  endTime: number,
  topNu: number,
  projectId: string,
  topMethod: string,
  topTimeStep: number
) => {
  return fetch(`${prefix}/metrics/gateway/projects`, {
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      projectId,
      topNu,
      topMethod,
      topTimeStep,
    },
  });
};

export const getProjectIdList = () => {
  return fetch(`${prefix}/security/project`);
};

// 获取查询模板数据
export const getQueryTemplateData = (
  metricsTypes: string[],
  startTime: number,
  endTime: number,
  topNu: number,
  dslMd5: string,
  topMethod: string,
  topTimeStep: number
) => {
  return fetch(`${prefix}/metrics/gateway/dsl`, {
    method: POST,
    body: {
      metricsTypes,
      startTime,
      endTime,
      dslMd5,
      topNu,
      topMethod,
      topTimeStep,
    },
  });
};

export const getDslMd5List = (startTime: number, endTime: number) => {
  return fetch(`${prefix}/metrics/gateway/dsl-md5?startTime=${startTime}&endTime=${endTime}`);
};
