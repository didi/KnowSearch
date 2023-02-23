import { getCookie, getCurrentProject } from "lib/utils";
import { request, post, put, delete as deleteMethod } from "knowdesign/lib/utils/request";
const esPrefix = "/api/es/admin";
const logiPrefix = "/v3/security";
const rolePrefix = "/role";
const prefix = `${esPrefix}${logiPrefix}${rolePrefix}`;

const getHeaders = () => {
  return {
    "X-SSO-USER": getCookie("userName") || "",
    "X-SSO-USER-ID": getCookie("userId") || "",
    "X-LOGI-SECURITY-PROJECT-ID": getCurrentProject()?.id || "",
  };
};

// 分页查询角色列表
export const queryRoleList = (params): any => {
  return post(`${prefix}/page`, params, { headers: getHeaders() });
};

// 获取角色详情
export const queryRoleDetail = (id: any): any => {
  return request(`${prefix}/${id}`, { headers: getHeaders() });
};

// 创建角色&&更新角色信息
export const createOrUpdateRole = (isCreate: boolean, params: any) => {
  const reqMethod = isCreate ? post : put;
  return reqMethod(`${prefix}`, params, { headers: getHeaders() });
};

// 删除角色
export const deleteRole = (id: any) => {
  return deleteMethod(`${prefix}/${id}`, { headers: getHeaders() });
};

// 分配角色
export const assignRole = (params: any) => {
  return post(`${prefix}/assign`, params, { headers: getHeaders() });
};

export const queryRoleStatus = (id: any): any => {
  return deleteMethod(`${prefix}/delete/check/${id}`, { headers: getHeaders() });
};

// 角色管理/分配用户/列表
export const queryAssignedUserByRole = (roleId): any => {
  return request(`${prefix}/assign/${roleId}`, { headers: getHeaders() });
};

export const queryPermissionTree = () => {
  return request(`${esPrefix}${logiPrefix}/permission/tree`, { headers: getHeaders() });
};

// 获取资源own角色权限树
export const getRolePermission = (): any => {
  return request(`${esPrefix}${logiPrefix}/permission/resource-owner`, { headers: getHeaders() });
};

// 从角色中删除该角色下的用户
export const deleteUserByRoleId = (id: number, userId: number) => {
  return deleteMethod(`${prefix}/${id}/user/${userId}`, { headers: getHeaders() });
};
