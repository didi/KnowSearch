/*
*@运维管理 => 操作记录
*/
export interface IOpRecord {
  bizId: string;
  content?: string;
  id: number;
  module: string;
  moduleId: number;
  operate: string;
  operateId: number;
  operateTime: string;
  operator: string;
}

/*
*@运维管理 => 版本管理
*/
export interface IVersions {
  id: number;
  name: string;
  createTime: string;
  creator: string;
  deleteFlag: boolean;
  esVersion: string;
  manifest: number;
  release: string;
  updateTime: string;
  url: string;
  key?: number;
  value?: string;
  label?: string;
}

/*
*@运维管理 => 配置管理
*/
export interface IDeploy {
  createTime: string;
  dimension?: number | string;
  id: number;
  status: number;
  memo?: string;
  updateTime?: string;
  value: string;
  valueGroup: string;
  valueName: string;
}

export interface IDeploySwitch {
  status: number;
  id: number;
}

export interface ITemplateLogic {
  appId: number;
  createTime: string;
  authType: number;
  dataCenter: string;
  dataType: number;
  dateField?: string;
  dateFieldFormat?: string;
  dateFormat?: string;
  desc: string;
  expression: string;
  expireTime?: number;
  hotTime: number;
  id: number;
  idField: string;
  libraDepartment: string;
  libraDepartmentId: string;
  name: string;
  quota: number;
  responsible: string;
  routingField?: string;
  updateTime: string;
  [key: string]: any;
}

export interface IOpPhysicsCluster {
  cluster: string;
  clusterConfigTemplate?: string;
  dataCenter: string;
  desc: string;
  ecmClusterId?: string;
  esVersion: string;
  httpAddress: string;
  httpWriteAddress: string;
  idc: string;
  id: number;
  level: number;
  password?: string;
  readAddress: string;
  templateSrvs?: string;
  type: number;
  writeAddress: string;
  name?: string;
  clusterStatus: string;
}

/*
*@运维管理 => 集群管理 => 物理集群 => 配置信息
*/

export interface IPhyConfig {
  clusterId: number;
  enginName: string;
  typeName: string;
  configData?: string;
  desc?: string;
  versionConfig?: any;
  versionTag?: any;
  selected?: any;
  id: number;
}


export interface INewRegionRacks {
  cluster: string;
  rack: string;
  usageFlags: number;
}

export interface INewRegionRacksMap {
  title: string;
  value?: string;
  key: string;
  disabled: boolean;
  description: string;
}

export interface IRegionTask {
  deltaRacks?: string;
  finishTime: string;
  regionCostCpuCount: number;
  regionCostDiskG: number;
  id: number;
  regionId: number;
  srcRacks: string;
  startTime: string;
  status: number;
  task: number;
  type: number;
}

export interface IRegionTaskItem {
  actualCpuCount: number;
  actualDiskG: number;
  cluster: string;
  combinedCpuCount: number;
  combinedDiskG: number;
  createTime: string;
  dateFormat: string;
  expireTime: number;
  expression: string;
  hotDay: number;
  id: number;
  maxQueryTime: number;
  maxScrollTime: number;
  maxTps: number;
  physicalId: number;
  quota: number;
  quotaCpuCount: number;
  quotaDiskG: number;
  replicaNum: number;
  shardNum: number;
  sumDocCount: number;
  sumIndexSizeG: number;
  taskId: number;
  templateName: string;
}
