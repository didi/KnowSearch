import { request, post, put } from "../../Utils/request";

export const getProjectList = (): any => {
  return request(`/security/api/v1/project/list`);
};

export const getResourceTypeList = (): any => {
  return request(`/security/api/v1/resource/type/list`);
};

export const getResourceList = (params): any => {
  return post(`/security/api/v1/resource/mbr/page`, params);
};

export const getResourceUserList = (params): any => {
  return post(`/security/api/v1/resource/mbr/list`, params);
};
export const assignUser = (params): any => {
  return post(`/security/api/v1/resource/mbr/assign`, params);
};

export const getDeptTree = (): any => {
  return request(`/security/api/v1/dept/tree`);
};

export const getDeptResourceList = (params): any => {
  return post(`/security/api/v1/resource/mbu/page`, params);
};

export const getUserResourceTypeList = (params): any => {
  return post(`/security/api/v1/resource/mbu/list`, params);
};

export const assignResource = (params): any => {
  return post(`/security/api/v1/resource/mbu/assign`, params);
};

export const getStatus = (): any => {
  return request(`/security/api/v1/resource/vpc/status`);
};

export const updataStatus = (): any => {
  return put(`/security/api/v1/resource/vpc/switch`);
};

export const batchAssign = (params): any => {
  return post(`/security/api/v1/resource/assign/batch`, params);
};
