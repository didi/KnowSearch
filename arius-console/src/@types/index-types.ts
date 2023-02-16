export interface IQuotaUsage {
  actualCpuCount: number;
  actualDiskG: number;
  quotaCpuCount: number;
  quotaCpuUsage: number;
  quotaDiskG: number;
  quotaDiskUsage: number;
  cpuPercent?: number;
  diskGPercent?: number;
}
export interface IIndex {
  key?: number;
  name: string;
  authType: number;
  cluster: string;
  createTime: string;
  dataCenter: string;
  dataType: number;
  desc: string;
  id: number;
  quotaUsage: IQuotaUsage;
  value: number;
  libraDepartment: string;
  hasDCDR: boolean;
  logicId: number;
}

export interface INodeDivide {
  ip: string;
  logicClusterName: string;
  clusterLogicNames: string;
  rack: string;
  regionId: number | string;
  role: number;
  status: number;
}

export interface IInput {
  order: number;
  name: string;
}

export interface INode {
  description?: string;
  inputs?: IInput[];
  name: string;
  order: string;
  position?: string;
}
export interface IPartition {
  docsCount: number;
  docsDeleted: number;
  health: string;
  index: string;
  pri: number;
  priStoreSize: string;
  rep: number;
  status: string;
  storeSize: string;
}

export interface IZoomInfo {
  currentUsage: IQuotaUsage;
  cyclicalRoll: boolean;
  dateField: string;
  expireTime: number;
  id: number;
  name: string;
  quota: number;
  topUsage: IQuotaUsage;
  [key: string]: any;
}

export interface IOpTemplateIndex {
  cluster: string;
  config: string;
  createTime: string;
  expression: string;
  id: number;
  logicId: number;
  name: string;
  rack?: string;
  role: number;
  shard: number;
  shardRouting: number;
  status: number;
  version: number;
}

export interface IOpTemplateIndexDetail {
  cluster: string;
  config: string;
  createTime: string;
  expression: string;
  id: number;
  logicId: number;
  name: string;
  rack: string;
  role: number;
  shard: number;
  shardRouting: number;
  status: number;
  version: number;
}
