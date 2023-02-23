import { getCookie } from "knowdesign/lib/utils/tools";
import { request, post } from "knowdesign/lib/utils/request";
import { getCurrentProject } from "../../../lib/utils";
const prefix = `/api/es/admin/v3/security`;

const getHeaders = () => {
  return {
    "X-SSO-USER": getCookie("userName") || "",
    "X-SSO-USER-ID": getCookie("userId") || "",
    "X-LOGI-SECURITY-PROJECT-ID": getCurrentProject()?.id || "",
  };
};

export const queryUserList = (params): any => {
  return post(`${prefix}/user/page`, params, { headers: getHeaders() });
};

export const queryRoleListByName = (roleName = "") => {
  return request(`${prefix}/role/list/${roleName ? "?roleName=" + roleName : ""}`, { headers: getHeaders() });
};

export const queryAssignedRoleByUser = (userId: number, roleName = ""): any => {
  return request(`${prefix}/user/assign/${userId}/${roleName}`, { headers: getHeaders() });
};

export const queryUserDetail = (id): any => {
  return request(`${prefix}/user/${id}`, { headers: getHeaders() });
};

export const assignRoleToUser = (params: any): any => {
  console.log();
  return post(`${prefix}/role/assign`, params, { headers: getHeaders() });
};

export const queryDeptTreeData = (val): any => {
  return request(`${prefix}/dept/tree`, { headers: getHeaders() });
};

export const deleteUser = (id): any => {
  return request(`${prefix}/user/${id}`, { headers: getHeaders(), method: "DELETE" });
};
