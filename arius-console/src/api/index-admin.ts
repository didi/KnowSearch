import fetch from "../lib/fetch";

const v3Prefix = "/v3";

interface indexAdminType {
  page: number;
  size: number;
  cluster?: string;
  index?: string;
  health?: string;
  orderByDesc?: boolean;
  sortTerm?: string;
}

// 获取索引管理数据
export const getIndexAdminData = (data: indexAdminType) => {
  return fetch(`${v3Prefix}/indices/page`, {
    method: "POST",
    body: data,
  });
};

export const addIndexAdmin = (data: any) => {
  return fetch(`${v3Prefix}/indices`, {
    method: "POST",
    body: data,
    returnRes: true,
  });
};

interface delListType {
  clusterPhyName: string;
  index: string;
}

export const delIndexAdminData = (delList: delListType[]) => {
  return fetch(`${v3Prefix}/indices`, {
    method: "DELETE",
    body: delList,
  });
};

interface configType {
  cluster: string;
  index: string;
  type?: string;
  value?: boolean;
}

export const openOrCloseReadOrWrite = (configList: configType[]) => {
  return fetch(`${v3Prefix}/indices/block`, {
    method: "PUT",
    body: configList,
  });
};

export const indicesClose = (configList: configType[]) => {
  return fetch(`${v3Prefix}/indices/close`, {
    method: "PUT",
    body: configList,
  });
};

export const indicesOpen = (configList: configType[]) => {
  return fetch(`${v3Prefix}/indices/open`, {
    method: "PUT",
    body: configList,
  });
};

// 获取索引shard分配详情
export const getShardDetail = (cluster: string, index: string) => {
  return fetch(`${v3Prefix}/indices/${cluster}/${index}/shard`);
};

export const getMapping = (cluster: string, index: string) => {
  return fetch(`${v3Prefix}/indices/${cluster}/${index}/mapping`);
};

export const updateMapping = (params) => {
  return fetch(`${v3Prefix}/indices/mapping`, {
    method: "PUT",
    body: params,
  });
};

export const getSetting = (cluster: string, index: string) => {
  return fetch(`${v3Prefix}/indices/${cluster}/${index}/setting`);
};

export const updateSetting = (params) => {
  return fetch(`${v3Prefix}/indices/setting`, {
    method: "PUT",
    body: params,
  });
};

export const getIndexDetail = (cluster: string, index: string) => {
  return fetch(`${v3Prefix}/indices/${cluster}/${index}`);
};

export const setAlias = (params) => {
  return fetch(`${v3Prefix}/indices/alias`, {
    method: "PUT",
    body: params,
  });
};

export const deleteAlias = (params) => {
  return fetch(`${v3Prefix}/indices/alias`, {
    method: "DELETE",
    body: params,
  });
};

export const executeRollover = (params) => {
  return fetch(`${v3Prefix}/indices/srv/rollover`, {
    method: "POST",
    body: params,
  });
};

export const executeForceMerge = (params) => {
  return fetch(`${v3Prefix}/indices/srv/force-merge`, {
    method: "POST",
    body: params,
  });
};

export const executeShrink = (params) => {
  return fetch(`${v3Prefix}/indices/srv/shrink`, {
    method: "POST",
    body: params,
  });
};

export const executeSplit = (params) => {
  return fetch(`${v3Prefix}/indices/srv/split`, {
    method: "POST",
    body: params,
  });
};
