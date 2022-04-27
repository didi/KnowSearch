export interface IUser {
  domainAccount?: string;
  chineseName: string;
  department: string;
  label?: string;
  value?: string;
  role: number;
  oldPassword: string;
  roleName?: string;
  key?: number;
  status?: number;
  mobile?: string;
  email?: string;
  name?: string;
}

export interface IAppDetail {
  name: string;
  department: string;
  responsible: string;
  searchType: number;
  id: number;
  showVerifyCode: boolean;
  cluster?: string;
  departmentId: string;
  isRoot: number;
  memo: string;
  queryThreshold: number;
  verifyCode: string;
  dataCenter: string;
}
