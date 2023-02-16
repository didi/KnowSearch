import { IPermission } from "store/type";
import actionTypes from "./actionTypes";

export const setIsAdminUser = (isAdminUser: boolean) => ({
  type: actionTypes.SET_USER_TYPE,
  payload: {
    isAdminUser,
  },
});

export const setGlobalUserInfo = (userInfo: any) => ({
  type: actionTypes.SET_USER,
  payload: {
    userInfo,
  },
});

export const setUserPermissionTree = (permissionTree: IPermission) => ({
  type: actionTypes.SET_USER_PERMISSION,
  payload: {
    permissionTree,
  },
});
