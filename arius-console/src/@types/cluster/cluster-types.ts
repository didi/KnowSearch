export interface ICluster {
  projectId: string;
  appName: string;
  dataNodesNumber: number;
  authId: number;
  authType: number;
  associatedPhyClusterName: string;
  clusterConfigTemplate?: string;
  clusterStatus: IClusterStatus;
  dataCenter: string;
  esClusterConfigTemplateVO?: string;
  esRoleClusterVOS: IEsRoleCluster[];
  podNumber: number | string;
  id: number;
  level: number;
  libraDepartment: string;
  libraDepartmentId: string;
  logicCluster: boolean;
  memo: string;
  name: string;
  quota: number;
  type: number;
  permissions: string;
  status?: string;
  authCode?: number;
}

export interface IClusterInfo extends ICluster {
  [key: string]: any;
}
export interface IClusterStatus {
  name: string;
  status: string;
  docNu: number;
  id: number;
  totalDisk: number;
  usedDisk: number;
}

export interface IClusterNode {
  hosts: string;
  machineSpec: string;
  nu: number;
  pidcount: number;
}

export interface INode {
  create_time?: string;
  delete_flag?: number;
  id: number;
  role: string;
  spec: string;
  value?: string;
  label?: string;
  key?: number;
}

/*
 *@集群管理 => 操作记录
 */
export interface IOperaRecordt {
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
 *@集群管理 => 物理集群
 */
export interface IOpClusterCreate {
  cluster: string;
  dataCenter: string;
  desc: string;
  esVersion: string;
  httpAddress: string;
  httpWriteAddress: string;
  idc: string;
  level: number;
  type: number;
}

export interface IOpLogicCluster {
  projectId: number;
  name: string;
  authId: number;
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
  updateTime: string;
  createTime: string;
  clusterStatus: IClusterStatus;
  status: string;
  permissions: string;
  esRoleClusterVOS: IEsRoleCluster[];
}

export interface IOpLogicClusterDetail {
  projectId: number;
  clusterConfigTemplate?: string;
  configJson: string;
  dataCenter: string;
  esClusterConfigTemplateVO?: any;
  id: string;
  level: string;
  libraDepartment: string;
  libraDepartmentId: string;
  memo: string;
  name: string;
  quota: number;
}

export interface IOpPhysicsCluster {
  usename?: string;
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
  currentAppAuth: number;
  logicClusterAndRegionList?: any;
  supportZeus?: boolean;
}

export interface IOpPhysicsClusterDetail {
  cluster: string;
  clusterConfigTemplate: string;
  clusterStatus: string;
  createTime: string;
  dataCenter: string;
  desc: string;
  diskUsage: number;
  esClusterTemplateSrvVOS: IEsClusterTemplate[];
  esRoleClusterVOS: IEsRoleCluster[];
  ecmClusterId: number;
  esVersion: string;
  httpAddress: string;
  httpWriteAddress: string;
  id: number;
  idc: string;
  level: number;
  password: string;
  readAddress: string;
  type: number;
  updateTime: string;
  writeAddress: string;
  dataNodeNu?: string;
  pidCount: number;
  [key: string]: any;
}

export interface IEsClusterTemplate {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}

export interface IEsRoleCluster {
  createTime: string;
  elasticClusterId: number;
  esRoleClusterHostVO: IEsRoleClusterHost[];
  id: number;
  pidCount: number;
  podNumber: number;
  role: string;
  roleClusterName: string;
  updateTime: string;
  machineSpec: string;
}

export interface IRoleIpList {
  hostname: string;
  role: string;
  beCold: boolean;
}

export interface IEsRoleClusterHost {
  createTime: string;
  hostname: string;
  id: number;
  role: string;
  roleClusterId: number;
  updateTime: string;
}

export interface IOpClusterRoles {
  cfgId: number;
  createTime: string;
  deleteFlag: boolean;
  elasticClusterId: number;
  esVersion: string;
  id: number;
  machineSpec: string;
  pidCount: number;
  plugIds: string;
  podNumber: number;
  role: string;
  roleClusterName: string;
  updateTime: string;
  key?: number;
  value?: string;
  label?: string;
}

export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}

export interface ITemplateSrvData {
  item: ITemplateSrv;
  disabled: boolean;
  status: number; // 0: 已开启 1: 未开启
}

export interface ICreatePhyCluster {
  type: number;
  phyClusterName: string;
  dataCenter: string;
  nsTree: string;
  idc?: string;
  esVersion: string;
  plugs?: string;
  creator?: string;
  desc: string;
  roleClusters?: IRoleCluster[];
  pidCount?: number;
  machineSpec?: string;
  level?: string;
  roleClusterHosts?: IRoleClusterHots[];
  tags?: string;
}

export interface IRoleCluster {
  role: string;
  podNumber: number;
  pidCount: number;
  machineSpec: string;
}

export interface IRoleClusterHots {
  hostname: string;
  role: string;
}

export interface IOpExpandValues {
  clientNode: string;
  dataNode: string;
  description: string;
  masterNode: string;
  operationType: number;
  phyClusterName: string;
  pidCount: number;
  type: number;
}

export interface IRegion {
  region: string;
  racks: string;
  key: string;
}

export interface IAccessClusterRegion {
  value: number;
  regionList: IRegion[];
}
