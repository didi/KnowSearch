import fetch, { formFetch } from "../lib/fetch";

const prefix = `/v3/op`;

// 获取脚本分页列表接口
export const getScriptList = (params) => {
  return fetch(`${prefix}/script/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 新增脚本接口
export const addScript = (params) => {
  const { name, describe, uploadFile, timeout } = params;
  const formData = new FormData();
  formData.append("name", name);
  formData.append("describe", describe || "");
  formData.append("uploadFile", uploadFile);
  formData.append("timeout", timeout || "");
  return formFetch(`${prefix}/script`, {
    method: "POST",
    body: formData,
  });
};

// 修改脚本接口
export const updateScript = (params) => {
  const { id, describe, uploadFile, timeout } = params;
  const formData = new FormData();
  formData.append("describe", describe || "");
  formData.append("id", id || "");
  formData.append("timeout", timeout || "");
  if (uploadFile) {
    formData.append("uploadFile", uploadFile);
  }
  return formFetch(`${prefix}/script/update`, {
    method: "POST",
    body: formData,
  });
};

// 是否正在使用脚本
export const checkScriptUsing = (id: number) => {
  return fetch(`${prefix}/script/${id}/using`);
};

// 删除脚本接口
export const delScript = (id: number) => {
  return fetch(`${prefix}/script/${id}`, {
    method: "DELETE",
  });
};

// 获取安装包分页列表接口
export const getSoftwareList = (params) => {
  return fetch(`${prefix}/software/package/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

// 新增安装包接口
export const addSoftware = (params) => {
  const { groupConfigList, describe, uploadFile, name, scriptId, version, packageType } = params;
  const formData = new FormData();
  formData.append("groupConfigList", JSON.stringify(groupConfigList));
  formData.append("describe", describe || "");
  formData.append("uploadFile", uploadFile);
  formData.append("name", name);
  formData.append("scriptId", scriptId);
  formData.append("version", version);
  formData.append("packageType", packageType);
  formData.append("isEnginePlugin", packageType === 3 ? "0" : "1");

  return formFetch(`${prefix}/software/package`, {
    method: "POST",
    body: formData,
  });
};

// 修改安装包接口
export const updateSoftware = (params) => {
  const { groupConfigList, describe, uploadFile, scriptId, id, packageType, version, name } = params;
  const formData = new FormData();
  formData.append("groupConfigList", JSON.stringify(groupConfigList));
  formData.append("describe", describe || "");
  formData.append("scriptId", scriptId);
  formData.append("version", version);
  formData.append("name", name);
  formData.append("id", id);
  formData.append("packageType", packageType);
  formData.append("isEnginePlugin", packageType === 3 ? "0" : "1");
  if (uploadFile) {
    formData.append("uploadFile", uploadFile);
  }
  return formFetch(`${prefix}/software/package/update`, {
    method: "POST",
    body: formData,
  });
};

// 根据安装包id获取安装包接口
export const getSoftware = (id: number) => {
  return fetch(`${prefix}/software/package/${id}`);
};

// 获取脚本名称list
export const getScriptNameList = () => {
  return fetch(`${prefix}/script/list`);
};

// 是否正在使用安装包
export const checkSoftwareUsing = (id: number) => {
  return fetch(`${prefix}/software/package/${id}/using`);
};

// 删除安装包接口
export const delSoftware = (id: number) => {
  return fetch(`${prefix}/software/package/${id}`, {
    method: "DELETE",
  });
};
