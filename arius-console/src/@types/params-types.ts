export interface IWorkOrder {
  contentObj: object;
  dataCenter?: string;
  submitor?: string;
  submitorAppid: number;
  description?: string;
  type?: string;
}

export interface IOperatRecord {
  bizId?: string | number;
  operator?: string;
  moduleId?: number | string;
}

export interface IOpPackageParams {
  creator: string;
  desc: string;
  esVersion: string;
  manifest: number;
  url: string;
  fileName: string;
  uploadFile?: any;
  id?: string;
  md5: string;
}

export interface IOpClusterRegion {
  logicClusterId: number;
  configJson: string;
  id: number;
  racks: string;
  share: number;
}

export interface IClearIndexParams {
  logicId: number;
  delIndices: string[];
  delQueryDsl: string;
}

export interface IPhysicalTemplateParams {
  cluster?: string;
  role?: number;
  logicId?: number;
}
