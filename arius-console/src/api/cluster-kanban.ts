import { indexConfigData } from 'container/indicators-kanban/cluster-kanban/node-view-config';
import fetch from '../lib/fetch';
const Prefix = "admin";
const POST = "POST";

// 获取项目下的物理集群名称列表
export const getClusterNameList = () => {
  return fetch("/v3/op/phy/cluster/names", {
    prefix: Prefix
  });
}

type secondMetricsType = 'overview' | "node" | "index" | "template";
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
export const getNodeViewData = async (metricsTypes: string[], clusterPhyName: string, startTime: number, endTime: number, topNu: number, nodeIp: string[]) => {
  if (!clusterPhyName) {
    return;
  }
  // 为了兼容task类型的新指标 在请求方式里兼容
  const taskList = [];
  const aggTypes = [];
  let taskData = [];
  let data = [];
  metricsTypes = metricsTypes.filter(item => {
    if (indexConfigData[item] && indexConfigData[item].newquota) {
      taskList.push(item);
      aggTypes.push(indexConfigData[item].newquota);
      return false;
    } else {
      return true;
    }
  })
  if (taskList && taskList.length) {
    taskData = await fetch("/v3/op/phy/cluster/metrics/node/task", {
      prefix: Prefix,
      method: POST,
      body: {
        aggTypes: aggTypes,
        metricsTypes: taskList,
        clusterPhyName,
        startTime,
        endTime,
        topNu,
        nodeNames: nodeIp
      }
    });
  }
  if (metricsTypes && metricsTypes.length) {
    data = await fetch("/v3/op/phy/cluster/metrics/nodes", {
      prefix: Prefix,
      method: POST,
      body: {
        "aggType": "avg",
        metricsTypes,
        clusterPhyName,
        startTime,
        endTime,
        topNu,
        nodeNames: nodeIp
      }
    });
  }
  return [...data, ...taskData];
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

// 获取index视图数据
export const getTemplateViewData = (metricsTypes: string[], clusterPhyName: string, startTime: number, endTime: number, topNu: number, logicTemplateId: string, aggType) => {
  if (!clusterPhyName) {
    return;
  }
  return fetch("/v3/op/phy/cluster/metrics/template", {
    prefix: Prefix,
    method: POST,
    body: {
      aggType,
      metricsTypes,
      clusterPhyName,
      startTime,
      endTime,
      topNu,
      logicTemplateId
    }
  });
}

// 获取index视图 index 列表数据
export const getIndexNameList = (clusterPhyName) => {
  return fetch(`/v3/op/phy/cluster/metrics/${clusterPhyName}/indices`, { prefix: Prefix });
}

// 获取索引模板 索引模板 列表数据
export const getListTemplates = (clusterPhyName) => {
  return fetch(`/v3/op/template/logic/listTemplates?cluster=${clusterPhyName}`, { prefix: Prefix });
}

// 获取chartTable
export const getChartTableList = (clusterPhyName, node, time) => {
  return fetch(`/v3/op/phy/cluster/metrics/${clusterPhyName}/${node}/task?startTime=${time[0]}&endTime=${time[1]}`, { prefix: Prefix });
}