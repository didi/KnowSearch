import { request, post, put, delete as deleteMethod } from "../../Utils/request";

export const queryUserList = (params): any => {
  return post(`/v1/logi-security/user/page`, params);
};

export const queryRoleListByName = (roleName = "") => {
  return request(`/v1/logi-security/role/list/${roleName ? "?roleName=" + roleName : ""}`);
};

export const queryAssignedRoleByUser = (userId: number, roleName = ""): any => {
  return request(`/v1/logi-security/user/assign/list/${userId}/${roleName}`);
};

export const queryUserDetail = (id): any => {
  return request(`/v1/logi-security/user/${id}`);
};

export const assignRoleToUser = (params: any): any => {
  return post(`/v1/logi-security/role/assign`, params);
};

export const queryDeptTreeData = (val): any => {
  return request("/v1/logi-security/dept/tree");
};
