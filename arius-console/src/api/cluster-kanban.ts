import { indexConfigData } from "container/indicators-kanban/cluster-kanban/node-view-config";
import fetch from "../lib/fetch";
import { isSuperApp } from "lib/utils";
const Prefix = "admin";
const v3Prefix = "/v3";
const POST = "POST";

export const getLogicClusterNames = () => {
  return fetch(`${v3Prefix}/cluster/logic/names`, {
    errorNoTips: true,
  });
};

type secondUserConfigType = "overview" | "node" | "index" | "template";
type aggType = "max" | "avg";

// 获取账号下已配置指标列表
export const getCheckedList = (secondUserConfigType: secondUserConfigType) => {
  return fetch(`${v3Prefix}/metrics/cluster/config-metrics`, {
    method: "POST",
    body: {
      userName: "",
      firstUserConfigType: "cluster",
      secondUserConfigType,
      userConfigTypes: [],
    },
  });
};

// 设置账号下已配置指标列表
export const setCheckedList = (secondUserConfigType: secondUserConfigType, userConfigTypes: string[]) => {
  return fetch(`${v3Prefix}/metrics/cluster/config-metrics`, {
    method: "PUT",
    body: {
      userName: "",
      firstUserConfigType: "cluster",
      secondUserConfigType,
      userConfigTypes,
    },
  });
};

// 获取总览视图数据 /v3/metrics/cluster/overview
export const getOverviewData = (metricsTypes: string[], clusterPhyName: string, startTime: number, endTime: number) => {
  if (!clusterPhyName) {
    return;
  }
  let body = {
    aggType: "max",
    metricsTypes,
    clusterPhyName,
    startTime,
    endTime,
  } as any;
  const superApp = isSuperApp();
  if (!superApp) {
    delete body.clusterPhyName;
    body.clusterLogicName = clusterPhyName;
  }
  return fetch(`${v3Prefix}/metrics/cluster/overview`, {
    method: POST,
    body,
  });
};

// 获取节点视图数据 /v3/metrics/cluster/nodes
export const getNodeViewData = async (
  metricsTypes: string[],
  clusterPhyName: string,
  startTime: number,
  endTime: number,
  topNu: number,
  nodeIp: string[],
  topMethod: string,
  topTimeStep: number
) => {
  if (!clusterPhyName) {
    return;
  }
  // 为了兼容task类型的新指标 在请求方式里兼容
  const taskList = [];
  const aggTypes = [];
  let taskData = [];
  let data = [];
  const superApp = isSuperApp();
  metricsTypes = metricsTypes.filter((item) => {
    if (indexConfigData[item] && indexConfigData[item].newquota) {
      taskList.push(item);
      aggTypes.push(indexConfigData[item].newquota);
      return false;
    } else {
      return true;
    }
  });
  if (taskList && taskList.length) {
    let body = {
      aggTypes: aggTypes,
      metricsTypes: taskList,
      clusterPhyName,
      startTime,
      endTime,
      topNu,
      nodeNames: nodeIp,
      topMethod,
      topTimeStep,
    } as any;
    if (!superApp) {
      delete body.clusterPhyName;
      body.clusterLogicName = clusterPhyName;
    }
    taskData = await fetch(`${v3Prefix}/metrics/cluster/node/task`, {
      method: POST,
      body,
    });
  }
  if (metricsTypes && metricsTypes.length) {
    let body = {
      aggType: "avg",
      metricsTypes,
      clusterPhyName,
      startTime,
      endTime,
      topNu,
      nodeNames: nodeIp,
      topMethod,
      topTimeStep,
    } as any;
    if (!superApp) {
      delete body.clusterPhyName;
      body.clusterLogicName = clusterPhyName;
    }
    data = await fetch(`${v3Prefix}/metrics/cluster/nodes`, {
      method: POST,
      body,
    });
  }
  return [...data, ...taskData];
};

// 获取节点视图数据 Ip 名称列表
export const getNodeIpList = (clusterPhyName: string) => {
  const superApp = isSuperApp();
  return fetch(`${v3Prefix}/cluster/${superApp ? "phy" : "logic"}/node/${clusterPhyName}/names`, { prefix: Prefix });
};

// 获取节点视图数据 Ip 名称列表
export const getNodeInfoList = (clusterPhyName: string) => {
  const superApp = isSuperApp();
  return fetch(`${v3Prefix}/cluster/${superApp ? "phy" : "logic"}/node/${clusterPhyName}/infos`);
};

// 获取index视图数据
export const getIndexViewData = (
  metricsTypes: string[],
  clusterPhyName: string,
  startTime: number,
  endTime: number,
  topNu: number,
  indexNames: string[],
  topMethod: string,
  topTimeStep: number
) => {
  if (!clusterPhyName) {
    return;
  }
  const superApp = isSuperApp();
  let body = {
    aggType: "avg",
    metricsTypes,
    clusterPhyName,
    startTime,
    endTime,
    topNu,
    indexNames,
    topMethod,
    topTimeStep,
  } as any;
  if (!superApp) {
    delete body.clusterPhyName;
    body.clusterLogicName = clusterPhyName;
  }
  return fetch(`${v3Prefix}/metrics/cluster/indices`, {
    method: POST,
    body,
  });
};

// 获取索引模板视图数据 /v3/metrics/cluster/template
export const getTemplateViewData = (
  metricsTypes: string[],
  clusterPhyName: string,
  startTime: number,
  endTime: number,
  topNu: number,
  templateIdList: string[],
  aggType,
  topMethod: string,
  topTimeStep: number
) => {
  if (!clusterPhyName) {
    return;
  }
  const superApp = isSuperApp();
  let body = {
    aggType,
    metricsTypes,
    clusterPhyName,
    startTime,
    endTime,
    topNu,
    templateIdList,
    topMethod,
    topTimeStep,
  } as any;
  if (!superApp) {
    delete body.clusterPhyName;
    body.clusterLogicName = clusterPhyName;
  }
  return fetch(`${v3Prefix}/metrics/cluster/templates`, {
    method: POST,
    body,
  });
};

// 获取index视图 index 列表数据
export const getPhyIndexNameList = (clusterPhyName) => {
  return fetch(`${v3Prefix}/indices/${clusterPhyName}/phy/indices`);
};

export const getLogicIndexNameList = (logicClusterName) => {
  return fetch(`${v3Prefix}/indices/${logicClusterName}/logic/indices`);
};

// 获取索引模板 索引模板 列表数据
export const getPhyListTemplates = (clusterPhyName) => {
  return fetch(`${v3Prefix}/template/logic/${clusterPhyName}/phy/templates`);
};

export const getLogicListTemplates = (logicClusterName) => {
  return fetch(`${v3Prefix}/template/logic/${logicClusterName}/logic/templates`);
};

// 获取chartTable
export const getChartTableList = (clusterPhyName, node, time) => {
  return fetch(`${v3Prefix}/metrics/cluster/${clusterPhyName}/${node}/task?startTime=${time[0]}&endTime=${time[1]}`, {
    prefix: Prefix,
  });
};

// 通过model筛选获取指标字典信息
export const getDictionary = (params) => {
  return fetch(`${v3Prefix}/metrics/dictionary`, {
    method: "POST",
    body: params,
  });
};
