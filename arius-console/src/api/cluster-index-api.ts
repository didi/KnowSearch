import { IUNSpecificInfo } from "typesPath/base-types";
import fetch from "../lib/fetch";
import store from "store";

const app = store.getState().app;
export interface IAllIndexList {
  page: number;
  size: number;
  //权限
  // authType: number;
  id?: number;
  // 名称
  name?: string;
  health?: string;
  //类型
  dataType?: number;
  projectId?: number;
  resourceId?: number;
  cluster?: string;
  // hasDCDR?: boolean;
  desc?: string;
  sortTerm?: string;
  orderByDesc?: boolean;
  hasDCDR?: any;
  openSrv?: number;
  showMetadata?: boolean;
}

export interface ISwitchMasterSlave {
  templateIds: number[];
  type: number;
  timeout?: number;
}

// 获取模板列表
export const getAllIndexList = (params: IAllIndexList) => {
  return fetch(`/v3/template/logic/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const getTimeFormat = () => {
  return fetch(`/v3/config/time-format`);
};

export const getIndexDataType = () => {
  return fetch(`/v3/template/logic/data-type`);
};

export const getIndexBaseInfo = (id: number) => {
  return fetch(`/v3/template/detail/${id}`);
};

export const getIndexPartitionList = (id: number) => {
  return fetch(`/v3/template/cyclical-roll?logicId=${id}`);
};

export const deleteIndexInfo = (id: number) => {
  return fetch(`/v3/template/delete-info/${id}`, {
    method: "DELETE",
  });
};

// 校验集群是否能创建模板
export const getClusterCheck = (logicClusterId: number) => {
  return fetch(`/v3/cluster/logic/${logicClusterId}/check-region-not-empty`, {
    errorNoTips: true,
    returnRes: true,
  });
};

export const createIndex = (params: any) => {
  return fetch(`/v3/template/logic`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const updateIndexInfo = (params: any) => {
  return fetch(`/v3/template`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

export const getClearInfo = (logicId: number) => {
  return fetch(`/v3/template/logic/indices?logicId=${logicId}`);
};

// 模板清理
export const clearIndex = (logicId, delIndices) => {
  return fetch(`/v3/template/logic/indices`, {
    method: "DELETE",
    body: { logicId, delIndices },
  });
};

// 模板扩缩容
export const updateShard = (templateId, shardNum) => {
  return fetch(`/v3/template/logic/${templateId}/${shardNum}/adjust-shard`, {
    method: "PUT",
  });
};

// 模板升版本
export const updateVision = (templateId) => {
  return fetch(`/v3/template/logic/${templateId}/upgrade`, {
    method: "PUT",
  });
};

export const getIndexMappingInfo = (logicId: number) => {
  return fetch(`/v3/template/schema?logicId=${logicId}`);
};

export const updateIndexMappingInfo = (params: IUNSpecificInfo) => {
  return fetch(`/v3/template/schema`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

// 获取索引模板setting
export const getSetting = (logicId: number) => {
  return fetch(`/v3/template/setting?logicId=${logicId}`);
};

export const updateIndexSettingInfo = (params: IUNSpecificInfo) => {
  return fetch(`/v3/template/setting/${params.logicId}`, {
    method: "PUT",
    body: JSON.stringify(params.params),
  });
};

export const setTemplateIndexSetting = (params: any) => {
  return fetch(`/v3/template/logic/template-index-setting`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

//获取物理集群索引列表
export const getPhyIndexNameList = (clusterPhyName: string) => {
  return fetch(`/v3/indices/${clusterPhyName}/phy/indices`);
};
//获取逻辑集群索引列表
export const getLogIndexNameList = (clusterLogicName: string) => {
  return fetch(`/v3/indices/${clusterLogicName}/logic/indices`);
};

export const setIndexSetting = (params: any) => {
  return fetch(`/v3/indices/mergeSettings`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

export const toDsl = (phyClusterName: string, sql: string) => {
  return fetch(`/v3/gateway/sql${phyClusterName ? "/" + phyClusterName : ""}/explain`, {
    method: "POST",
    prefix: "sql",
    body: sql,
  });
};

export const explainSql = (sql: string, phyClusterName?: string) => {
  return fetch(`/v3/gateway/sql${phyClusterName ? "/" + phyClusterName : ""}`, {
    method: "POST",
    prefix: "sql",
    body: sql,
  });
};

// 模板扩缩容信息
export const getTemplateIndexDetail = (id: number) => {
  return fetch(`/v3/templates/physical/${id}`);
};

// 检查模板名称
export const getNameCheck = (templateName: string) => {
  return fetch(`/v3/template/logic/${templateName}/name-check`, {
    errorNoTips: true,
    returnRes: true,
  });
};

// 检查索引名称
export const checkIndexName = (cluster: string, index: string) => {
  return fetch(`/v3/indices/${cluster}/${index}/exists`, {
    errorNoTips: true,
    returnRes: true,
  });
};

// DCDR主从切换接口
export const switchMasterSlave = (params: ISwitchMasterSlave) => {
  return fetch(`/v3/dcdr/work-order/task/switch-master-slave`, {
    method: "POST",
    body: params,
  });
};

// 模版服务列表
export const getServiceList = (params: IAllIndexList) => {
  return fetch(`/v3/template/srv/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 模版服务各操作
export const updateTemplateSrv = (params: any, method = "PUT") => {
  return fetch(`/v3/template/srv/${params.srvCode}/${params.templateIdList}`, {
    method: method,
    body: JSON.stringify(params.params),
  });
};

// 创建DCDR链路
export const createDCDR = (params: any) => {
  const { templateId, regionId, targetCluster } = params;
  return fetch(`/v3/template/dcdr/${templateId}/${regionId}/${targetCluster}`, {
    method: "POST",
  });
};

// 查看DCDR链路
export const getDCDRDetail = (id: number) => {
  return fetch(`/v3/template/dcdr/${id}`, { returnRes: true });
};

// 删除DCDR链路
export const deleteDCDR = (id: number) => {
  return fetch(`/v3/template/dcdr/${id}`, {
    method: "DELETE",
  });
};

// 创建DCDR链路时集群下拉列表
export const getDCDRCluster = (id: number) => {
  return fetch(`/v3/cluster/phy/${id}/same-version-and-dcdr-plugin/cluster-names`);
};

// 创建DCDR链路时region下拉列表
export const getDCDRRegion = (cluster: string) => {
  return fetch(`/v3/cluster/phy/region/${cluster}/dcdr`);
};

// 不同应用下的逻辑集群
export const getClusterPerApp = () => {
  return fetch("/v3/cluster/logic/ids-names");
};

// 不同应用下的物理集群
export const getPhyClusterPerApp = () => {
  return fetch(`/v3/cluster/phy/names`, {
    errorNoTips: true,
  });
};

// 不同应用下按type区分的物理集群
export const getPhyClusterPerType = (type?: number) => {
  return fetch(`/v3/cluster/phy/${type}/names`);
};

// 写变更
export const disableWrite = (templateId: any, status) => {
  return fetch(`/v3/template/srv/${templateId}/block-write?status=${status}`, {
    method: "PUT",
  });
};

// 读变更
export const disableRead = (templateId: any, status) => {
  return fetch(`/v3/template/srv/${templateId}/block-read?status=${status}`, {
    method: "PUT",
  });
};
