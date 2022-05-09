import { request, post, put, delete as deleteMethod } from "../../Utils/request";

export const queryProjectList = (params): any => {
  return post(`/v1/logi-security/project/page`, params);
};

export const queryProjectDetail = (id): any => {
  return request(`/v1/logi-security/project/${id}`);
};

export const createOrUpdateProject = (isCreate: boolean, params): any => {
  const reqMethod = isCreate ? post : put;
  return reqMethod("/v1/logi-security/project", params);
};

export const deleteProject = (id: number): any => {
  return deleteMethod(`/v1/logi-security/project/${id}`);
};

export const switchProjectStatus = (id: number): any => {
  return put(`/v1/logi-security/project/switch/${id}`);
};

export const queryProjectStatus = (id: number): any => {
  return request(`/v1/logi-security/project/delete/check/${id}`);
};

export const queryUserList = (name = ""): any => {
  return request(`/v1/logi-security/user/list/${name}`);
};

export const queryDeptTreeData = (): any => {
  return request("/v1/logi-security/dept/tree");
};
