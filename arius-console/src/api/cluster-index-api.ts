import { IUNSpecificInfo } from 'typesPath/base-types';
import { IClearIndexParams, IWorkOrder } from 'typesPath/params-types';
import fetch from '../lib/fetch';
import store from 'store';

const app = store.getState().app
export interface IAllIndexList {
  page: number;
  size: number;
  //权限
  authType: number;
  //类型
  dataType: number;
  // 索引名称
  name: number;
  clusterPhies?: string[];
  sortTerm?: string;
  orderByDesc?: boolean;
  hasDCDR?: boolean;
}

export interface ISwitchMasterSlave {
  templateIds: number[],
  type: number,
}

/**
 * 获取索引列表
 */

export const getIndexList = (clusterId: number) => {
  return fetch(`/v2/console/cluster/logicTemplates?clusterId=${clusterId}&appId=${app.appInfo()?.id || -1}`);
};

export const getAllIndexList = (params: IAllIndexList) => {
  // return fetch(`/v2/console/template/list?appId=${appId}`);
  return fetch(`/v3/op/template/logic/page`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const getIndexBaseInfo = (id: number) => {
  return fetch(`/v2/console/template/get?logicId=${id}`);
};

export const getIndexPartitionList = (id: number) => {
  return fetch(`/v2/console/template/cyclicalRoll?logicId=${id}`);
};

export const getIndexCapacity = (id: number) => {
  return fetch(`/v2/console/template/capacity?logicId=${id}`);
};

export const getIndexQuotaCost = (quota: number) => {
  return fetch(`/v2/quota/template/cost?quota=${quota}`);
};

export const queryQuotaCost = (diskG: number, clusterId: number) => {
  return fetch(`/v2/quota/template/cost?diskG=${diskG}&clusterId=${clusterId}`);
};

export const getAllClusterList = () => {
  return fetch(`/v2/console/cluster/listAll`);
};

export const getIndexDeleteInfo = (id: number) => {
  return fetch(`/v2/console/template/deleteInfo?logicId=${id}`);
};

export const deleteIndexInfo = (id: number) => {
  return fetch(`/v2/console/template/deleteInfo?logicId=${id}`, {
    method: 'DELETE',
  });
};

export const createIndex = (type: string, params: IWorkOrder) => {
  return fetch(`/v3/normal/order/${type}/submit`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const updateIndexInfo = (params: any) => {
  return fetch(`/v2/console/template/update`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const getClearInfo = (logicId: number) => {
  return fetch(`/v2/console/template/clearInfo?logicId=${logicId}`);
};

export const clearIndex = (params: IClearIndexParams) => {
  return fetch(`/v2/console/template/clearInfo`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const getIndexMappingInfo = (logicId: number) => {
  return fetch(`/v2/console/template/schema?logicId=${logicId}`);
};

export const checkIndexMappingInfo = (value: object) => {
  return fetch(`/v2/template/mapping/check`, {
    method: 'POST',
    body: JSON.stringify(value),
  });
};

export const updateIndexMappingInfo = (params: IUNSpecificInfo) => {
  return fetch(`/v2/console/template/schema`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

// 运维侧接口

export const getOpIndexList = (clusterId: number) => {
  return fetch(`/v2/op/resource/logicTemplates?clusterId=${clusterId}&appId=${app.appInfo()?.id || -1}`); // TODO: 待确定
};

export const getIndexNameList = () => {
  return fetch(`/v2/console/template/indices/list?appId=${app.appInfo()?.id}`);
};

export const getMappingList = (cluster: string, index: string) => {
  return fetch(`/v2/console/index/mapping/get?cluster=${cluster}&index=${index}`);
};

export const toDsl = (sql: string) => {
  return fetch(`/v3/op/gateway/sql/explain`, {
    method: 'POST',
    prefix: 'sql',
    body: sql,
  });
  // return fetch(`/_sql/explain`, {
  //   method: 'POST',
  //   prefix: 'sql',
  //   body: sql,
  // });
};

export const explainSql = (sql: string, phyClusterName?: string) => {
  return fetch(`/v3/op/gateway/sql${phyClusterName ? '/' + phyClusterName : ''}`, {
    method: 'POST',
    prefix: 'sql',
    body: sql,
  });
};

// 物理集群索引
export const getPhysicalTemplateIndexDetail = (id: number) => {
  // return fetch(`/v2/op/template/physical/get?physicalId=${id}`);
  // 索引末班
  return fetch(`/v3/op/template/physical/${id}`);
};

export const updateTemplateIndex = (params: any) => {
  // return fetch(`/v2/op/template/physical/upgrade`, {
  return fetch(`/v3/op/template/physical/multipleUpgrade`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const editTemplateIndex = (params: any) => {
  // return fetch(`/v2/op/template/physical/edit`, {
  // 新的编辑接口
  return fetch(`/v3/op/template/physical/multipleEdit`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const physicalCopy = (params: any) => {
  return fetch(`/v2/op/template/physical/copy`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const delTemplatePhysical = (id: number) => {
  return fetch(`/v2/op/template/physical/del?physicalId=${id}`, {
    method: 'DELETE',
  });
};

export const updateAppAuth = (params: any) => {
  return fetch(`/v2/op/app/auth/update`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const updateBinCluster = (id: number) => {
  return fetch(`/v2/op/app/auth/${id}`, {
    method: 'DELETE',
  });
}

export const getNameCheck = (templateName: string) => {
  // 检查名称
  return fetch(`/v3/op/template/logic/${templateName}/nameCheck`);
};

export const getSizeCheck = (logicClusterId: number, diskSize: number) => {
  // 校验模板大小资源是否充足
  return fetch(`/v3/op/logic/cluster/${logicClusterId}/${diskSize}/sizeCheck`);
};

export const checkEditMapping = (templateId: number) => {
  // 检查mapping是否可编辑
  return fetch(`/v3/op/template/logic/${templateId}/checkEditMapping/`, {
    errorNoTips: true,
    returnRes: true
  });
};

// /rollover/switch/{templateLogicId}/{disable} 
// disable :是否禁用rollover能力（false 启用，true 禁用）
export const rolloverSwitch = (templateLogicId: number, disable: string) => {
  return fetch(`/v3/op/template/logic/rollover/switch/${templateLogicId}/${disable}`, {
    method: 'PUT',
  });
};

export const getClusterTemplateSrv = (clusterLogicId: number) => {
  return fetch(`/v3/op/logic/cluster/templateSrv/${clusterLogicId} `);
};

// /v3/op/template/logic/dcdr/switchMasterSlave DCDR主从切换接口
export const switchMasterSlave = (params: ISwitchMasterSlave) => {
  return fetch(`/v3/op/dcdr/work-order/task/switchMasterSlave`, {
    method: 'POST',
    body: params,
  });
};

export const disableRead = (params: any) => {
  return fetch(`/v2/op/template/logic/blockRead`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const disableWrite = (params: any) => {
  return fetch(`/v2/op/template/logic/blockWrite`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};