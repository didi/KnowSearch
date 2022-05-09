export interface ITask {
  clusterNodeRole: string;
  createTime: string;
  creator: string;
  deleteFlag: boolean;
  id: number;
  orderType: number;
  physicClusterId: number;
  status: string;
  title: string;
  type: string;
  updateTime: string;
  workOrderId: number;
  esVersion: string;
  key?: number;
  taskType: number;
  businessKey: number;
  expandData: string;
}

export interface ITaskBaseInfo {
  clusterName: string;
  clusterNodeRole: string;
  createTime: string;
  creator: string;
  dept: string;
  desc: string;
  esVersion: string;
  id: number;
  idc: string;
  imageName: string;
  nsTree: string;
  orderType: number;
  status: string;
  title: string;
  type: number;
  updateTime: string;
  workOrderId: number;
}

export interface ITaskDetail {
  cancel: number;
  creating: number;
  roleNameTaskDetailMap: ITaskNodeMap;
  failed: number;
  ignore: number;
  orderType: number;
  percent: number;
  status: string;
  success: number;
  sum: number;
  waiting: number;
  key?: number;
}

export interface IDcdrStepDetail {
  logicTemplateId: number;
  createTime: string;
  logicTemplateName: string;
  logicClusterId: number;
  logicClusterName: string;
  masterPhysicalTemplateName: string;
  masterPhysicalClusterName: string;
  vicePhysicalTemplateName: string;
  vicePhysicalClusterName: string;
  status: string;
  taskProgress: number;
  content: string;
}

export interface IDcdrStepDetailInfo extends IDcdrStepDetail {
  [key: string]: any;
}
export interface ITaskNodes {
  header: string;
  data: INodeTask[];
  key: string;
}

export interface ITaskNodeMap {
  clientnode: INodeTask[];
  datanode: INodeTask[];
  masternode: INodeTask[];
  coldnode: INodeTask[];
}

export interface INodeTask {
  grp: number;
  hostname: string;
  id: number;
  idx: number;
  role: string;
  status: string;
  taskId: number;
  workOrderTaskId: number;
  updateTime: string;
  key?: number;
}

export interface ITaskLog {
  agent: string;
  user: string;
}
