import { IOpClusterRegion } from 'typesPath/params-types';
import fetch, { formFetch } from '../lib/fetch';

export const getLogicClusterRegionList = (areaId: number) => {
  return fetch(`/v3/op/logic/cluster/region/list?logicClusterId=${areaId}`);
};

// GET /v3/op/phy/cluster/region 获取物理集群region列表接口
export const getPhyClusterRegionList = (clusterName: string, clusterLogicType) => {
  return fetch(`/v3/op/phy/cluster/region?cluster=${clusterName}&clusterLogicType=${clusterLogicType}`);
};

// /v3/op/phy/cluster/region/bind 获取物理集群region列表接口
export const getPhyClusterRegionListBind = (clusterName: string, clusterLogicType, clusterLogicId) => {
  return fetch(`/v3/op/phy/cluster/region/bind?cluster=${clusterName}&clusterLogicType=${clusterLogicType}&clusterLogicId=${clusterLogicId}`);
};

// 逻辑集群新建集群 获取想同版本物理集群列表
export const getPhyClusterList = (clusterLogicType, clusterName) => {
  return fetch(`/v3/op/phy/cluster/${clusterLogicType}/${clusterName}/version/list`);
}

export const delLogicCluterRegion = (id: number, logicClusterId: number) => {
  return fetch(`/v3/op/logic/cluster/region/delete?logicClusterId=${logicClusterId}&regionId=${id}`, {
    method: 'DELETE',
  });
};

export const getClusterRegionTaskList = (id: number) => {
  return fetch(`/v2/op/capacity/plan/area/region/task/list?regionId=${id}`);
};

export const getClusterRegionTaskItemList = (id: number) => {
  return fetch(`/v2/op/capacity/plan/area/region/task/listItem?taskId=${id}`);
};

export const clusterRegionCheck = (id: number) => {
  return fetch(`/v2/op/capacity/plan/area/region/check?regionId=${id}`, {
    method: 'PUT',
  });
};

export const clusterRegionPlan = (id: number) => {
  return fetch(`/v2/op/capacity/plan/area/region/plan?regionId=${id}`, {
    method: 'PUT',
  });
};

export const clusterRegionMoveShard = (id: number) => {
  return fetch(`/v2/op/capacity/plan/area/region/moveShard?regionId=${id}`, {
    method: 'PUT',
  });
};

export const clusterRegionDelete = (id: number) => {
  return fetch(`/v2/op/capacity/plan/area/region/delete?regionId=${id}`, {
    method: 'DELETE',
  });
};

export const clusterRegionEdit = (params: IOpClusterRegion) => {
  return fetch(`/v2/op/capacity/plan/area/region/edit`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const clusterRegionNew = (params: IOpClusterRegion) => {
  return fetch(`/v3/op/phy/cluster/region/add`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const regionTaskExecute = (id: number) => {
  return fetch(`/v2/op/capacity/plan/area/region/task/execute?taskId=${id}`, {
    method: 'PUT',
  });
};

export const openPhyClusterRegionsCapPlan = (clusterName: string) => {
  return fetch(`/v3/op/capacity/plan/phyCluster/open?phyClusterName=${clusterName}`, {
    method: 'PUT',
  });
};

export const closePhyClusterRegionsCapPlan = (clusterName: string) => {
  return fetch(`/v3/op/capacity/plan/phyCluster/close?phyClusterName=${clusterName}`, {
    method: 'PUT',
  });
};

export const logicClusterBinRegion = (params: any) => {
  return fetch(`/v3/op/logic/cluster/region`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const getPhyClusterRacks = (cluster: string) => {
  return fetch(`/v3/op/phy/cluster/region/phyClusterRacks?cluster=${cluster}`);
};

export const getBindRack = (clusterPhy: string, clusterLogic: string, templateSize: number) => {
  // /v3/op/phy/cluster/{clusterPhy}/{clusterLogic}/{templateSize}/bindRack
  return fetch(`/v3/op/phy/cluster/${clusterPhy}/${clusterLogic}/${templateSize}/bindRack`);
};
