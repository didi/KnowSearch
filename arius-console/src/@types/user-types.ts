export interface IUser {
  domainAccount?: string;
  chineseName: string;
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
export interface NewIUser {
  deptId?: number;
  email?: string;
  id?: number;
  phone?: string;
  realName?: string;
  roleList?: string[];
  userName?: string;
}
