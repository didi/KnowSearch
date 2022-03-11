import { ILabelValue } from "typesPath/base-types"
import { IVersions } from "typesPath/cluster/physics-type"
import { INode } from "typesPath/index-types"
import { IDcdrStepDetailInfo, ITaskBaseInfo } from "typesPath/task-types"
import { IAppDetail } from "typesPath/user-types"

type UserState = {
  getName: Function;
}

export interface ModalState {
  loading: boolean;
  modalId: string;
  drawerId: string;
  params: any;
  cb?: Function;
}

export interface ClustrtData {
  cost: number | string,
  nodeList: INodeListObjet,
  packageList: { docker: IVersions[], host: IVersions[] },
}

export interface INodeListObjet {
  masternode: INode[];
  clientnode: INode[];
  datanode: INode[];
  datanodeceph: INode[];
}

type AppState = {
  appInfo: () => IAppDetail,
  appList: any[];
}

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
  menu: string,
  loading: boolean,
}

// 集群看板
export interface ClusterState {
  clusterName: string;
  startTime: number;
  endTime: number;
  isMoreDay: boolean;
}
// gateway 看板
export interface GatewayState {
  startTime?: number;
  endTime?: number;
  isMoreDay?: boolean;
  isUpdate?: boolean;
}