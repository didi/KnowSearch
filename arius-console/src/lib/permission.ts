import { IPermission } from "store/type";
import store from "store";

export const getPermissionTree = () => {
  let permissionTree = [] as IPermission[];
  try {
    permissionTree = store.getState().user.permissionTree || [];
  } catch (err) {
    //
  }
  return permissionTree;
}

export const hasOpPermission = (parentName: string, permissionName: string) => {
  let permissionTree = getPermissionTree();
  const target = permissionTree.find(item => item.permissionName === parentName)?.childList || [];

  return target.find(row => row.permissionName === permissionName)?.has;
};

export const getPagePermission = (permissionName: string, permissionTree: IPermission[]) => {
  return permissionTree.find(item => item.permissionName === permissionName)?.has;
};