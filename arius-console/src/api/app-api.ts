import fetch from "../lib/fetch";
import { getCookie } from "lib/utils";

const v3Prefix = `/v3`;
const logiSecurityProject = `/security/project`;

export const getNoCodeLoginAppList = (projectId: number) => {
  return fetch(`/v3/es-user/project/${projectId}`, {
    headers: {
      "X-ARIUS-APP-TICKET": "xTc59aY72",
    },
  });
};

export const getApp = (id: number) => {
  return fetch(`${v3Prefix}/project-config/${id}`);
};

export const getAppByProjectId = (id: number) => {
  return fetch(`${v3Prefix}/es-user/project/${id}`);
};

export const updateAppByProjectId = (params) => {
  return fetch(`${v3Prefix}/es-user/`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

export const createAppByProjectId = (id: number, params) => {
  return fetch(`${v3Prefix}/es-user/${id}`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const deleteOneAppByProjectId = (projectId: number, esUser: number) => {
  return fetch(`${v3Prefix}/es-user/${projectId}/${esUser}`, {
    method: "DELETE",
  });
};

// logi-security 接口迁移

export const getProjectListByUserId = (userId: number) => {
  return fetch(`${v3Prefix}${logiSecurityProject}/user/${userId}`);
};

export const getProjectMetaList = () => {
  return fetch(`${v3Prefix}${logiSecurityProject}`);
};

export const getProjectList = (params: any) => {
  return fetch(`${v3Prefix}${logiSecurityProject}/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const getUnassignedUsers = (projectId: number) => {
  let isAdmin = getCookie("isAdminUser") === "yes";
  return fetch(`${v3Prefix}${logiSecurityProject}/unassigned?id=${projectId}${isAdmin ? "&containsAdminRole=true" : ""}`);
};

export const getProject = (projectId: number) => {
  return fetch(`${v3Prefix}${logiSecurityProject}/${projectId}`);
};

export const createProject = (params) => {
  return fetch(`${v3Prefix}${logiSecurityProject}`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// /v3/security/project 更新项目
export const updateProject = (params) => {
  return fetch(`${v3Prefix}${logiSecurityProject}`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

// /v3/security/project/{id}/owner 从角色中增加该项目下的负责人
export const updateOwner = (id, params) => {
  return fetch(`${v3Prefix}${logiSecurityProject}/${id}/owner`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

// /v3/security/project/{id}/user 从角色中增加该项目下的用户
export const updateUser = (id, params) => {
  return fetch(`${v3Prefix}${logiSecurityProject}/${id}/user`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

export const deleteProject = (id: number) => {
  return fetch(`${v3Prefix}${logiSecurityProject}/${id}`, {
    method: "DELETE",
  });
};

// /v3/security/project/check-resources/{id} 检查一个项目的资源是否可用
export const checkResources = (id: number) => {
  return fetch(`${v3Prefix}/security/project/check-resources/${id}`);
};

export const checkBindGateway = () => {
  return fetch(`${v3Prefix}/security/project/check-cluster-bind-gateway`);
};
