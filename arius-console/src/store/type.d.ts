import { IProject } from "interface/project";
import { ILabelValue } from "typesPath/base-types";
import { IVersions } from "typesPath/cluster/physics-type";
import { INode } from "typesPath/index-types";
import { IDcdrStepDetailInfo, ITaskBaseInfo } from "typesPath/task-types";

export interface IPermission {
  has: boolean;
  id: number;
  leaf: boolean;
  parentId: number;
  permissionName: string;
  childList: IPermission[];
}

export interface IUser {
  userName: string;
  id: number;
  email: string;
  phone: string;
}

type UserState = {
  getName: (params: string) => any;
  isAdminUser: boolean;
  permissionTree: IPermission[];
  userInfo: IUser;
};

export interface ModalState {
  loading: boolean;
  modalId: string;
  drawerId: string;
  params: any;
  cb?: any;
}

export interface ClustrtData {
  cost: number | string;
  nodeList: INodeListObjet;
  packageList: { docker: IVersions[]; host: IVersions[] };
}

export interface INodeListObjet {
  masternode: INode[];
  clientnode: INode[];
  datanode: INode[];
  datanodeceph: INode[];
}

type AppState = {
  appInfo: () => IProject;
  projectList: any[];
};

export interface RegionState {
  phyClusterList: ILabelValue[];
  region: any[];
  racks: string;
  racksArr: ILabelValue[];
  type?: string;
  tableData?: any[];
}

export interface TaskState {
  dcdrStepDetail: IDcdrStepDetailInfo;
  taskBaseInfo: ITaskBaseInfo;
  menu: string;
  loading: boolean;
}

// 集群看板
export interface ClusterState {
  clusterName: string;
  startTime: number;
  endTime: number;
  isMoreDay: boolean;
  timeRadioKey?: string;
  isUpdate?: boolean;
}
// gateway 看板
export interface GatewayState {
  startTime?: number;
  endTime?: number;
  isMoreDay?: boolean;
  isUpdate?: boolean;
  timeRadioKey?: string;
}
// dashboard
export interface DashboardState {
  startTime: number;
  endTime: number;
  tabs: string;
  dymanicConfigMetrics: any[];
}
