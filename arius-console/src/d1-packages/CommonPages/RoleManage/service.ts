import { request, post, put, delete as deleteMethod } from "../../Utils/request";
const prefix = "/v1/logi-security/role";
export const queryRoleList = (params): any => {
  return post(`${prefix}/page`, params);
};

export const queryRoleDetail = (id: any): any => {
  return request(`${prefix}/${id}`);
};

export const createOrUpdateRole = (isCreate: boolean, params: any) => {
  const reqMethod = isCreate ? post : put;
  return reqMethod(`${prefix}`, params);
};

export const deleteRole = (id: any) => {
  return deleteMethod(`${prefix}/${id}`);
};

export const assignRole = (params: any) => {
  return post(`${prefix}/assign`, params);
};

export const queryUserListByName = (name) => {
  return request(`${prefix}/list/name/${name}`);
};

export const queryRoleStatus = (id: any): any => {
  return deleteMethod(`${prefix}/delete/check/${id}`);
};

export const queryAssignedUserByRole = (roleId, name = ""): any => {
  return request(`${prefix}/assign/list/${roleId}/${name}`);
};

export const queryPermissionTree = () => {
  return request("/v1/logi-security/permission/tree");
};
