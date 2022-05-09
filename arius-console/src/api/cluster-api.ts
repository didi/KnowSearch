import fetch, { formFetch } from '../lib/fetch';
import { IClusterInfo, IOpClusterCreate, IOpLogicClusterDetail, IOpPhysicsClusterDetail } from '../@types/cluster/cluster-types';
import { IOperatRecord, IOpPackageParams } from 'typesPath/params-types';
import store from 'store';
import { IDeploy, IDeploySwitch } from 'typesPath/cluster/physics-type';
import { IDeteilId } from 'typesPath/base-types';

const app = {
  currentAppInfo: {
    app: store.getState().app
  }
}

export interface ILogicLike {
  page: number;
  size: number;
  authType?: number;
  name?: string;
  health?: number;
  appId?: number;
  type?: number;
  sortTerm?: string;
  orderByDesc?: boolean;
  //todo：版本字段未给出
}

export interface IClusterList {
  page: number;
  size: number;
  authType?: number;
  cluster?: string;
  health?: number;
  esVersion?: string;
  sortTerm?: string;
  orderByDesc?: boolean;
  //todo：版本字段未给出
}
/**
 * cluster 相关接口
 *
 */

export const getAllClusterList = (appId: number) => {
  return fetch(`/v2/console/cluster/listAll?appId=${appId}`);
};

export const getClusterBaseInfo = (clusterId: number) => {
  return fetch(`/v2/console/cluster/get?clusterId=${clusterId}&appId=${app.currentAppInfo?.app.appInfo()?.id || -1}`);
};

export const getClusterCost = (quota: number, nodeSpecify: number) => {
  return fetch(`/v2/quota/cluster/cost?quota=${quota}&nodeSpecify=${nodeSpecify}`);
};

export const getNodeList = () => {
  return fetch(`/v2/console/cluster/machinespec/list`);
};

export const opAddLogicCluster = (params: IOpClusterCreate) => {
  return fetch(`/v2/op/resource/add`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};


export const opEditLogicCluster = (params: IOpLogicClusterDetail) => {
  return fetch(`/v2/op/resource/edit`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const opEditCluster = (params: IOpPhysicsClusterDetail) => {
  return fetch(`/v2/op/cluster/edit`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const getOpLogicClusterInfo = (resourceId: number) => {
  // return fetch(`/v2/op/resource/get?resourceId=${resourceId}`);
  return fetch(`/v3/op/logic/cluster/${resourceId}/overView`);
};


export const getPhysicsClusterDetail = (id: number) => {
  // return fetch(`/v2/op/cluster/get?clusterId=${id}`);
  return fetch(`/v3/op/phy/cluster/${id}/overView`);
};

export const createTomorrowIndex = (cluster: string) => {
  return fetch(`/v2/op/cluster/preCreateIndex?cluster=${cluster}`, {
    method: 'POST',
    body: JSON.stringify({}),
  });
};

export const collectPhysicsClusterNodeConfig = (cluster: string) => {
  return fetch(`/v2/op/cluster/collectClusterNodeSettings?cluster=${cluster}`, {
    method: 'POST',
    body: JSON.stringify({}),
  });
};

export const deleteExpiredIndex = (cluster: string) => {
  return fetch(`/v2/op/cluster/deleteExpireIndex?cluster=${cluster}`, {
    method: 'POST',
    body: JSON.stringify({}),
  });
};

export const getOpPhysicsClusterList = (params: IClusterList) => {
  return fetch(`/v3/op/phy/cluster/page`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const getCopyClusterPhyNames = (templatePhyId: string | number) => {
  return fetch(`/v3/op/template/physical/${templatePhyId}/copyClusterPhyNames`);
}

export const getRack = (clusterPhyName: string) => {
  return fetch(`/v3/op/phy/cluster/region/${clusterPhyName}/rack`);
}


export const getvailablePhysicsClusterList = (clusterLogicType: number, clusterLogicId: number) => {
  return fetch(`/v3/op/phy/cluster/${clusterLogicType}/${clusterLogicId}/list`);
};

export const getLoginClusterPhysicsClusterList = (clusterLogicId: number) => {
  return fetch(`/v3/op/phy/cluster/${clusterLogicId}/bind/version/list`);
};

export const getvailablePhysicsClusterListLogic = (clusterLogicType: number) => {
  return fetch(`/v3/op/phy/cluster/${clusterLogicType}/list`);
};


export const getOpLogicClusterList = (params: ILogicLike) => {
  return fetch(`/v3/op/logic/cluster/page`, {
    method: 'POST', // get会更好？
    body: JSON.stringify(params),
  });
};

// 全量的逻辑集群
export const getClusterList = () => {
  return fetch('/v3/op/logic/cluster/list');
}

// 新建模板集群列表
export const getClusterLogicNames = (type: number) => {
  return fetch(`/v3/op/logic/cluster/${type}`);
};

export const getPhysicClusterRoles = (physicClusterId: number) => {
  return fetch(`/v3/op/phy/cluster/${physicClusterId}/roles`);
};

export const getPhyClusterAvalibleTemplateSrv = (clusterName: string) => {
  return fetch(`/v3/op/phy/cluster/templateSrv/${clusterName}/select`);
};

export const getPhyClusterTemplateSrv = (clusterName: string) => {
  return fetch(`/v3/op/phy/cluster/templateSrv/${clusterName}`);
};

export const checkEditTemplateSrv = (templateId: number, templateSrvId: number) => {
  return fetch(`/v3/op/template/logic/${templateId}/${templateSrvId}/checkEditTemplateSrv/`, {
    errorNoTips: true,
    returnRes: true
  });
};

export const setPhysicsClusterTemplateSrv = (clusterName: string, templateSrvId: number) => {
  return fetch(`/v3/op/phy/cluster/templateSrv/${clusterName}/${templateSrvId}`, {
    method: 'PUT',
    body: JSON.stringify({}),
  });
};

export const deletePhysicsClusterTemplateSrv = (clusterName: string, templateSrvId: number) => {
  return fetch(`/v3/op/phy/cluster/templateSrv/${clusterName}/${templateSrvId}`, {
    method: 'DELETE',
    body: JSON.stringify({}),
  });
};

/*
 * 逻辑集群详情
 */

export const getLogicClusterAvalibleTemplateSrv = (clusterLogicId: string | number) => {
  return fetch(`/v3/op/logic/cluster/templateSrv/${clusterLogicId}/select`);
};


export const getLogicClusterTemplateSrv = (clusterLogicId: string | number) => {
  return fetch(`/v3/op/logic/cluster/templateSrv/${clusterLogicId}`);
};

export const setLogicClusterTemplateSrv = (clusterLogicId: string | number, templateSrvId: number) => {
  return fetch(`/v3/op/logic/cluster/templateSrv/${clusterLogicId}/${templateSrvId}`, {
    method: 'PUT',
    body: JSON.stringify({}),
  });
};

export const deleteLogicClusterTemplateSrv = (clusterLogicId: string | number, templateSrvId: number) => {
  return fetch(`/v3/op/logic/cluster/templateSrv/${clusterLogicId}/${templateSrvId}`, {
    method: 'DELETE',
    body: JSON.stringify({}),
  });
};
/*
 * 版本 相关接口
 */

export const getPackageList = () => {
  return fetch(`/v3/normal/ecm/package`);
};

export const addPackage = (params: IOpPackageParams) => {
  const { creator, desc, fileName, md5, esVersion, manifest, uploadFile, url } = params;
  const formData = new FormData();
  formData.append('url', url || '');
  formData.append('creator', creator);
  formData.append('pDefault', 'false');
  formData.append('esVersion', esVersion);
  formData.append('manifest', manifest + '');
  formData.append('desc', desc || '');
  if (uploadFile) {
    formData.append('uploadFile', uploadFile);
    formData.append('md5', md5);
    formData.append('fileName', fileName);
  }
  return formFetch(`/v3/normal/ecm/package`, {
    method: 'POST',
    body: formData,
  });
};

export const delPackage = (id: number) => {
  return fetch(`/v3/op/phy/cluster/package/${id}`, {
    method: 'DELETE',
  });
};

export const updatePackage = (params: IOpPackageParams) => {
  const { creator, desc, fileName, md5, esVersion, manifest, uploadFile, id, url } = params;
  const formData = new FormData();
  formData.append('creator', creator);
  formData.append('pDefault', 'false');
  formData.append('esVersion', esVersion);
  formData.append('manifest', manifest + '');
  formData.append('id', id);
  formData.append('desc', desc || '');
  if (url) {
    formData.append('url', url);
  }
  if (uploadFile) {
    formData.append('uploadFile', uploadFile);
    formData.append('md5', md5);
    formData.append('fileName', fileName);
  }
  return formFetch(`/v3/normal/ecm/package/update`, {
    method: 'POST',
    body: formData,
  });
};

/**
 * 操作记录 相关接口
 *
 */
export const getUserRecordList = (params: IOperatRecord) => {
  return fetch(`/v3/normal/record/list`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const getUserRecordMultiList = (params: any) => {
  return fetch(`/v3/normal/record/${params.bizId}/${params.moduleId}/multiList`, {
    method: 'POST',
    body: {}
  });
};

export const getlistModules = () => {
  return fetch(`/v2/op/record/listModules`);
};

/*
* 配置管理 相关接口
*
*/

export const getDeployList = (params: object) => {
  return fetch(`/v2/op/config/list`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const newDeploy = (params: IDeploy) => {
  return fetch(`/v2/op/config/add`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};

export const switchDeploy = (params: IDeploySwitch) => {
  return fetch(`/v2/op/config/switch`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const updateDeploy = (params: IDeploy) => {
  return fetch(`/v2/op/config/edit`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const deleteDeploy = (params: IDeteilId) => {
  return fetch(`/v2/op/config/del?id=${params.id}`, {
    method: 'DELETE',
  });
};

export const clusterJoin = (params: any) => {
  return fetch(`/v3/op/phy/cluster/join`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

/*
 * 应用管理 相关接口
 */
export const getAppList = () => {
  return fetch(`/v2/op/app/list`);
};


/**
 * 获取物理集群的动态配置项
 */
export const getDynamicConfig = (clusterName: string) => {
  return fetch(`/v3/op/cluster/dynamicConfig/getAll/${clusterName}`, {
    prefix: "admin",
  })
}

export const getClusterAttributes = (clusterName: string) => {
  return fetch(`/v3/op/cluster/dynamicConfig/getClusterAttributes/${clusterName}`, {
    prefix: "admin",
  })
}

export const updateDynamicConfig = (clusterName: string, key: string, value: string | string[]) => {
  return fetch(`/v3/op/cluster/dynamicConfig/update`, {
    prefix: "admin",
    body: {
      clusterName,
      key,
      value
    },
    returnRes: true
  })
}

// 接入集群时对于开启冷热分离的服务的校验
// 接口：/v3/op/phy/cluster/templateSrv/{templateSrvId}/templateServiceWhenJoin
export const templateServiceWhenJoin = (templateSrvId, params: IDeploy) => {
  // /v3/op/phy/cluster/join/{templateSrvId}/checkTemplateService
  return fetch(`/v3/op/phy/cluster/join/${templateSrvId}/checkTemplateService`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

// 获取索引模板setting 接口：/v2/console/template/setting 
export const getSetting = (logicId: number) => {
  return fetch(`/v3/op/template/logic/setting?logicId=${logicId}`);
};

// 获取索引模板setting 接口：/v2/console/template/setting 
export const setSetting = (params: any) => {
  return fetch(`/v3/op/template/logic/setting`, {
    method: 'PUT',
    body: JSON.stringify(params)
  });
};