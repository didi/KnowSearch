import fetch, { formFetch } from "../lib/fetch";
import { IOpLogicClusterDetail, IOpPhysicsClusterDetail } from "../@types/cluster/cluster-types";
import { IOperatRecord, IOpPackageParams } from "typesPath/params-types";
import store from "store";
import { IDeploy, IDeploySwitch } from "typesPath/cluster/physics-type";
const v3Prefix = `/v3`;

export interface ILogicLike {
  page: number;
  size: number;
  authType?: number;
  name?: string;
  health?: number;
  projectId?: number;
  type?: number;
  sortTerm?: string;
  orderByDesc?: boolean;
  id?: number | string;
  memo?: string;
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
  id?: number | string;
  desc?: string;
  logicClusterName?: string;
  //todo：版本字段未给出
}
/**
 * cluster 相关接口
 *
 */

export const opEditLogicCluster = (params: IOpLogicClusterDetail) => {
  return fetch(`/v3/cluster/logic`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

// 获取我的集群详细信息
export const getOpLogicClusterInfo = (clusterLogicId: number) => {
  return fetch(`/v3/cluster/logic/detail/${clusterLogicId}`);
};

// 获取物理集群详细信息
export const getPhysicsClusterDetail = (id: number) => {
  return fetch(`/v3/cluster/phy/${id}/overview`);
};

// 按条件分页获取物理集群列表
export const getOpPhysicsClusterList = (params: IClusterList) => {
  return fetch(`/v3/cluster/phy/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const getvailablePhysicsClusterListLogic = (clusterLogicType: number) => {
  return fetch(`/v3/cluster/phy/${clusterLogicType}/names`);
};

export const getPhysicsClusterList = (clusterLogicType: number) => {
  return fetch(`/v3/cluster/phy/${clusterLogicType}/can-associated-names`);
};

export const getOpLogicClusterList = (params: ILogicLike) => {
  return fetch(`/v3/cluster/logic/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

//新全量逻辑集群
export const newLogicClusterList = () => {
  return fetch("/v3/cluster/logic");
};

//新物理集群
export const newPhysicClusterList = () => {
  return fetch("/v3/cluster/phy/names");
};

export const newClusterList = () => {
  return fetch("/v3/cluster/logic/cluster-phy-relation");
};

// 新建模板逻辑集群列表
export const getClusterLogicNames = (type: number) => {
  return fetch(`/v3/cluster/logic/${type}`);
};

export const getPhysicClusterRoles = (physicClusterId: number) => {
  return fetch(`/v3/cluster/phy/${physicClusterId}/roles`);
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
  formData.append("url", url || "");
  formData.append("creator", creator);
  formData.append("pDefault", "false");
  formData.append("esVersion", esVersion);
  formData.append("manifest", manifest + "");
  formData.append("desc", desc || "");
  if (uploadFile) {
    formData.append("uploadFile", uploadFile);
    formData.append("md5", md5);
    formData.append("fileName", fileName);
  }
  return formFetch(`/v3/normal/ecm/package`, {
    method: "POST",
    body: formData,
  });
};

export const delPackage = (id: number) => {
  return fetch(`/v3/normal/ecm/package/${id}`, {
    method: "DELETE",
  });
};

export const updatePackage = (params: IOpPackageParams) => {
  const { creator, desc, fileName, md5, esVersion, manifest, uploadFile, url, id } = params;
  const formData = new FormData();
  formData.append("url", url || "");
  formData.append("creator", creator);
  formData.append("pDefault", "false");
  formData.append("esVersion", esVersion);
  formData.append("manifest", manifest + "");
  formData.append("desc", desc || "");
  formData.append("id", id || "");
  if (uploadFile) {
    formData.append("uploadFile", uploadFile);
    formData.append("md5", md5);
    formData.append("fileName", fileName);
  }
  return formFetch(`/v3/normal/ecm/package/update`, {
    method: "POST",
    body: formData,
  });
};

/**
 * 操作记录 相关接口
 *
 */
export const getUserRecordList = (params: IOperatRecord) => {
  return fetch(`/v3/record/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 获取所有模块
export const getlistModules = () => {
  return fetch(`/v3/record/module`);
};

// 获取触发方式
export const getTriggerWay = () => {
  return fetch(`/v3/record/trigger-way`);
};

// 获取操作类型
export const getOperationType = (moduleCode?: number) => {
  return fetch(`/v3/record/operation-type/${moduleCode}`);
};

/*
 * 配置管理 相关接口
 *
 */

export const getDeployList = (params: object = {}) => {
  return fetch(`/v3/config/list`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const newDeploy = (params: IDeploy) => {
  return fetch(`/v3/config/add`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const switchDeploy = (params: IDeploySwitch) => {
  return fetch(`/v3/config/switch`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const updateDeploy = (params: IDeploy) => {
  return fetch(`/v3/config`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

export const deleteDeploy = (id: number) => {
  return fetch(`/v3/config/${id}`, {
    method: "DELETE",
  });
};

export const clusterJoin = (params: any) => {
  return fetch(`/v3/cluster/phy/join`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

/**
 * 获取物理集群的动态配置项
 */
export const getDynamicConfig = (clusterName: string) => {
  return fetch(`/v3/cluster/phy/dynamic-config/${clusterName}`, {
    prefix: "admin",
  });
};

export const getClusterAttributes = (clusterName: string) => {
  return fetch(`/v3/cluster/phy/dynamic-config/attributes/${clusterName}`, {
    prefix: "admin",
  });
};

export const getClusterNode = (clusterId: number) => {
  return fetch(`/v3/cluster/phy/node/${clusterId}`);
};

export const updateDynamicConfig = (clusterName: string, key: string, value: string | string[]) => {
  return fetch(`/v3/cluster/phy/dynamic-config`, {
    prefix: "admin",
    method: "PUT",
    body: {
      clusterName,
      key,
      value,
    },
    returnRes: true,
  });
};

export const updateAllocation = (data) => {
  return fetch(`/v3/cluster/phy/dynamic-config/multi-cluster`, {
    method: "PUT",
    body: data,
  });
};

// 获取数据中心列表 接口：/v3/config/cluster-data-center
export const getDataCenter = () => {
  return fetch(`/v3/config/cluster-data-center`);
};

// 获取集群版本列表 接口：/v3/config/cluster-version
export const getClusterVersion = () => {
  return fetch(`/v3/config/cluster-version`);
};

// 获取集群资源类型列表 接口：/v3/config/cluster-resource-type
export const getResourceType = () => {
  return fetch(`/v3/config/cluster-resource-type`);
};

// 获取节点规格列表 接口：/v3/config/node-specification
export const getNodeSpecification = () => {
  return fetch(`/v3/config/node-specification`);
};

// 获取申请集群节点规格列表 接口：/v3/cluster/phy/node/machineSpecs
export const getAccessClusterNodeSpecification = () => {
  return fetch(`/v3/cluster/phy/node/machineSpecs`);
};

// 获取集群节点数列表 接口：/v3/config/cluster-node-count
export const getNodeCount = () => {
  return fetch(`/v3/config/cluster-node-count`);
};

// 【物理集群：zeus管控】 接口：/v3/cluster/phy/zeus-url
export const getZeusUrl = () => {
  return fetch(`/v3/cluster/phy/zeus-url`);
};

// 新建集群  接口：/v3/op-task/es-cluster-create
export const creatCluster = (params) => {
  return fetch(`/v3/op-task/es-cluster-create`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 集群扩容 接口： /v3/op-task/es-cluster-expand
export const clusterExpand = (params: any) => {
  return fetch(`/v3/op-task/es-cluster-expand`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 集群缩容 接口： /v3/op-task/es-cluster-shrink
export const clusterShrink = (params: any) => {
  return fetch(`/v3/op-task/es-cluster-shrink`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 集群升级 接口： /v3/op-task/es-cluster-upgrade
export const clusterUpgrade = (params: any) => {
  return fetch(`/v3/op-task/es-cluster-upgrade`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 集群重启 接口： /v3/op-task/es-cluster-restart
export const clusterRestart = (params: any) => {
  return fetch(`/v3/op-task/es-cluster-restart`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 配置变更 接口： /v3/op-task/cluster-config-add
export const clusterConfigAdd = (params: any) => {
  return fetch(`/v3/op-task/cluster-config-add`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 获取划分方式
export const getDivideType = (id: number) => {
  return fetch(`/v3/cluster/phy/region/divideType/${id}`);
};

// 获取可划分至region的节点信息
export const getRegionNode = (clusterId: number, divideType?: string) => {
  return fetch(`/v3/cluster/phy/node/${clusterId}/region/${divideType}`);
};

// 根据物理集群名称获region信息，包含region中的数据节点信息
export const getRegionList = (clusterName: string) => {
  return fetch(`/v3/cluster/phy/region/${clusterName}`);
};

// 根据物理集群名称获region信息，包含region中的数据节点信息
export const getRackRegionList = (clusterName: string, divideType: string) => {
  return fetch(`/v3/cluster/phy/region/divide/${divideType}/${clusterName}`);
};

// 节点划分且创建region
export const divideRegionCheck = (params: any) => {
  return fetch(`/v3/cluster/phy/node/divide-region-check`, {
    method: "POST",
    body: params,
  });
};

// 节点划分且创建region
export const divideRegion = (params: any) => {
  return fetch(`/v3/cluster/phy/node/divide-region`, {
    method: "POST",
    body: params,
  });
};

// 编辑region中的节点
export const editRegion = (params: any) => {
  return fetch(`/v3/cluster/phy/node/divide-region`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

// 删除物理集群region接口
export const deleteRegion = (id: number) => {
  return fetch(`/v3/cluster/phy/region/${id}`, {
    method: "DELETE",
  });
};

// gateway 管理简要信息
export const getGatewayBriefInfo = () => {
  return fetch(`/v3/gateway/brief-info`);
};

// 物理集群绑定 gateway 接口：/v3/cluster/phy/{clusterPhyId}/{gatewayClusterId}
export const bindGateway = (params: any) => {
  return fetch(`/v3/cluster/phy/bind-gateway`, {
    method: "POST",
    body: params,
  });
};

// 提示用户索引和模板的数量
export const getCount = (clusterId: number) => {
  return fetch(`/v3/cluster/logic/index-template-count/${clusterId}`);
};

// 下线集群
export const deleteLogic = (clusterId: number) => {
  return fetch(`/v3/cluster/logic/${clusterId}`, {
    method: "DELETE",
  });
};

// 获取预估磁盘大小
export const getDiskSize = (clusterLogicId: number, count: number) => {
  return fetch(`/v3/cluster/logic/estimated-disk-size/${clusterLogicId}/${count}`);
};

// 物理集群编辑按钮接口
export const improveOpEditCluster = (params: any) => {
  return fetch(`/v3/cluster/phy/`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

// 获取gateway列表数据接口
// export const GetGatewayList = (params: IOpPhysicsClusterDetail) => {
//   return fetch(`/v3/phy/cluster/${params.id}/gateway`), {
//     method: "PUT"
//   }
// }

// 物理集群删除
export const clusterDelete = (params) => {
  return fetch(`/v3/cluster/phy/${params.regionId}`, {
    method: "DELETE",
  });
};

// 快捷命令接口
// node_state分析
export const nodeState = (cluster: any) => {
  return fetch(`/v3/cluster/phy/${cluster}/node-state-analysis`, {
    method: "PUT",
    errorNoTips: true,
  });
};

// indices分布
export const indicesDistribute = (params: any) => {
  return fetch(`/v3/cluster/phy/indices-distribution`, {
    method: "POST",
    body: JSON.stringify(params),
    errorNoTips: true,
  });
};

// /v3/cluster/phy/shard-distribution shard分布
export const shardDistribute = (params: any) => {
  return fetch(`/v3/cluster/phy/shard-distribution`, {
    method: "POST",
    body: JSON.stringify(params),
    errorNoTips: true,
  });
};

// task任务分析
export const task_mission_analysis = (cluster: any) => {
  return fetch(`/v3/cluster/phy/${cluster}/task-mission-analysis`, {
    method: "PUT",
    errorNoTips: true,
  });
};

// pending task分析
export const pendTaskAnalysis = (cluster: any) => {
  return fetch(`/v3/cluster/phy/${cluster}/pending-task-analysis`, {
    method: "PUT",
    errorNoTips: true,
  });
};

// 热点线程分析
export const hotThreadAnalysis = (cluster: any) => {
  return fetch(`/v3/cluster/phy/${cluster}/hot-thread-analysis`, {
    method: "PUT",
    errorNoTips: true,
  });
};

// shard分配说明
export const shardAssignDescription = (cluster: any) => {
  //测试集群名字
  return fetch(`/v3/cluster/phy/${cluster}/shard-assignment-description`, {
    method: "PUT",
    errorNoTips: true,
  });
};

// 异常shard分配重试
export const abnormalShardRetry = (cluster: any) => {
  return fetch(`/v3/cluster/phy/${cluster}/abnormal-shard-allocation-retry`, {
    method: "PUT",
    errorNoTips: true,
    returnRes: true,
  });
};

// 清除fieldData内存
export const clearFieldDataMemory = (cluster: any) => {
  return fetch(`/v3/cluster/phy/${cluster}/clear-field-data-memory`, {
    method: "PUT",
    errorNoTips: true,
    returnRes: true,
  });
};

// 超级应用的物理集群名称列表
export const getSuperPhyClusterList = () => {
  return fetch(`/v3/cluster/phy/super-phy-names`);
};

// 超级应用的逻辑集群名称列表，可按物理集群区分
export const getSuperLogiClusterList = (phyClusterName?: string) => {
  return fetch(`/v3/cluster/logic/cluster-phy-logic-names${phyClusterName ? `?phyClusterName=${phyClusterName}` : ""}`);
};

// 普通应用的逻辑集群名称列表
export const getMyLogiClusterList = () => {
  return fetch(`/v3/cluster/logic/cluster-app-logic-names`);
};

// 获取sense操作记录
export const getSenseOperate = (params: any) => {
  return fetch(`/v3/record/sense-operate-record`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 设置sense操作记录
export const setSenseOperate = (params: any) => {
  return fetch(`/v3/record/sense-operate-record`, {
    method: "PUT",
    body: JSON.stringify(params),
    errorNoTips: true,
  });
};

/**
 * 物理集群配置接口
 */

// 按条件分页获取 cluster 配置列表
export const getPhysicsClusterConfigList = (clusterPhyId: number, params: any) => {
  return fetch(`${v3Prefix}/cluster/phy/config-file/${clusterPhyId}/config/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 集群配置编辑
export const updatePhyClusterConfig = (params: any) => {
  return fetch(`${v3Prefix}/op-task/es-cluster-config-edit`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 获取原生模式下项目对应的访问集群列表
export const getEsUserPrimitiveList = (projectId: number) => {
  return fetch(`${v3Prefix}/es-user/${projectId}/primitive/cluster/list`);
};

// 获取集群模式下项目对应的访问集群列表
export const getEsUserList = (projectId: number) => {
  return fetch(`${v3Prefix}/es-user/${projectId}/cluster/list`);
};

// 设置应用默认的ES_User
export const setDefaultDisplay = (projectId: number, esUser: string) => {
  return fetch(`${v3Prefix}/es-user/${projectId}/${esUser}/defaultDisplay`, {
    method: "PUT",
  });
};

// 获取版本信息
export const getPackageVersion = (type) => {
  return fetch(`${v3Prefix}/op/software/package/${type}/versions`);
};

// 获取插件升级版本
export const getHigherPacksgeVersion = (packageId, version) => {
  return fetch(`${v3Prefix}/op/software/package/${packageId}/higher-versions?currentVersion=${version}`);
};

// 获取物理集群列表
export const getPhyConditionList = () => {
  return fetch(`/v3/cluster/phy/condition-list`, {
    method: "POST",
    body: {},
  });
};

// 获取物理集群所对应的逻辑集群名
export const getLogicNameList = (phyClusterName: string) => {
  return fetch(`${v3Prefix}/cluster/logic/logic-cluster-by-phy-name?phyClusterName=${phyClusterName}`);
};

// 申请集群 获取节点规格列表
export const getNodeMachineSpecs = () => {
  return fetch(`/v3/cluster/phy/node/machineSpecs`);
};

// 新建集群获取集群版本
export const getPackageTypeDescVersion = (packageTypeDesc: string) => {
  return fetch(`/v3/op/software/package/${packageTypeDesc}/versions`);
};

// 根据 clusterPhyId 获取物理集群配置信息
export const getPhyConfigFile = (clusterPhyId: number) => {
  return fetch(`/v3/cluster/phy/config-file/${clusterPhyId}/configs`);
};

// 数据迁移获取源集群物理集群列表
export const getSourceClusterList = () => {
  return fetch(`/v3/op-task/fast-index/es-clusters/installed/fast-dump`);
};

// 数据迁移获取目标集群物理集群列表
export const getTargetClusterList = () => {
  return fetch(`/v3/op-task/fast-index/support/es-cluster/versions`);
};

// 数据迁移获取任务提交地址
export const getAddress = (name) => {
  return fetch(`/v3/op-task/fast-index/es-clusters/task-submit-address?sourceClusterName=${name}`);
};

// 获取ES集群插件列表
export const getPhyPlugins = (clusterPhyId: number) => {
  return fetch(`/v3/cluster/phy/plugins?clusterPhyId=${clusterPhyId}`);
};

// 通过插件名称获取配置组
export const getPackageGroupConfigsByName = (name: string, version, packageTypeDesc) => {
  return fetch(
    `/v3/op/software/package/package-name/package-group-configs?name=${name}&version=${version}&packageTypeDesc=${packageTypeDesc}`
  );
};

// 集群插件安装
export const installClusterPlug = (params) => {
  return fetch(`/v3/op-task/es-cluster-plug-install`, {
    method: "POST",
    body: params,
  });
};

// 根据 clusterPhyId 和configId获取可以回滚的配置信息
export const getRollbackConfig = (clusterPhyId, configId) => {
  return fetch(`/v3/cluster/phy/config-file/${clusterPhyId}/${configId}/rollback`);
};

// 集群升级回滚
export const clusterRollback = (params) => {
  return fetch(`/v3/op-task/es-cluster-rollback`, {
    method: "POST",
    body: params,
  });
};
