
interface IBase {
  appId: string;
  appName: string;
  appPrincipals: string;
  clusterId: number;
  clusterName: string;
  topicName: string;
  principals: string;
  description: string;
}

export interface ITypeEnums {
  message?: string;
  type?: string;
  label?: string;
  value?: string;
  key?: number;
}

export interface IBaseOrder extends IBase {
  peakBytesInPerSec: number;
  orderId: number;
  gmtModify: number;
  gmtCreate: number;
  orderStatus: number;
  approver: string;
  approvalOpinions: string;
  applicant: string;
  predictBytesIn: number;
  realBytesIn: number;
  regionBrokerIdList: any[];
  regionNameList: string[];
  statusStr?: string;
  replicaNum?: number;
  retentionTime?: number;
  peakBytesIn?: number;
  regions?: string;
  brokers?: string;
  gmtHandle: number;
  id: number;
  status: number;
  access: number;
  name: string;
  type: string;
  createTime?: string;
  title?: string;
  approverAppId: string;
}
export interface IOrderInfo {
  id: number;
  title: string;
  type: string;
  approverList: any[];
  applicant: any;
  gmtCreate: string;
  gmtHandle: string;
  opinion: string;
  status: number;
  detail: any;
  detailInfo?: any;
  description?: string;
  approvers?: string[];
  currentStep?: 0 | 1 | 2;
  createTime: string;
  finishTime: string;
  applicantAppId: number;
  appDeptName: string;
  outcome: string;
  applicantAppName: string;
}

export interface IApprovalOrder {
  assignee: string;
  checkAuthority: boolean;
  comment: string;
  orderId: number;
  outcome: string;
  contentObj: object;
}
