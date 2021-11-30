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

export interface ILogicIndex {
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
  responsible: string;
}