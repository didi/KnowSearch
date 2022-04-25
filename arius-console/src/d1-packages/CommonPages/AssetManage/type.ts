export interface Project {
  id: number;
  projectName: string;
}

export interface Resource {
  id: number;
  typeName: string;
}
export interface ResourceObj {
  id: string;
  projectCode: string;
  projectName: string;
  resourceTypeName: string;
  resourceName: string;
  adminUserCnt: number;
  viewUserCnt: number;
  projectId: number;
  resourceId: number;
  resourceTypeId?: number;
}

export interface UserObj {
  id: string;
  username: string;
  realName: string;
  deptInfo: string;
  adminResourceCnt: number;
  viewResourceCnt: number;
  userId: number;
}

export interface IDrawerProps {
  title?: string;
  width?: number;
  data?: any;
  drawerKey: string;
  onClose: () => void;
  visible: boolean;
  reloadData: (data?: any) => void;
  footer?: () => React.ReactNode;
}

export interface TreeData {
  key: string;
  title: string;
  icon?: any;
  children?: TreeData[];
}

export interface UserInfo {
  hasLevel: number;
  realName: string;
  userId: number;
  username: string;
}
