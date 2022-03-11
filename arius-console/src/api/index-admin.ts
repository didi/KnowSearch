import fetch from '../lib/fetch';

const Prefix = "admin";
const POST = "POST";

interface indexAdminType {
  from: number,
  size: number,
  clusterPhyName?: string,
  index?: string,
  status?: string,
}

// 获取索引管理数据
export const getIndexAdminData = (data: indexAdminType) => {
  return fetch(`/v3/indices/page`, {
    method: "POST",
    body: data
  });
}

interface delListType {
  "clusterPhyName": string,
  "index": string
}

export const delIndexAdminData = (delList: delListType[]) => {
  return fetch(`/v3/indices`, {
    method: "DELETE",
    body: delList
  });
}

interface configType {
  "clusterPhyName": string,
  "index": string,
  "type"?: string,
  "value"?: boolean
}

export const openOrCloseReadOrWrite = (configList: configType[]) => {
  return fetch(`/v3/indices/block`, {
    method: "PUT",
    body: configList
  })
}

export const indicesClose = (configList: configType[]) => {
  return fetch(`/v3/indices/close`, {
    method: "PUT",
    body: configList
  })
}

export const indicesOpen = (configList: configType[]) => {
  return fetch(`/v3/indices/open`, {
    method: "PUT",
    body: configList
  })
}

// 获取索引shard分配详情
export const getShardDetail = (clusterPhyName: string, indexName: string) => {
  return fetch(`/v3/indices/${clusterPhyName}/${indexName}/shard`);
}

export const getMapping = (clusterPhyName: string, indexName: string) => {
  return fetch(`/v3/indices/${clusterPhyName}/${indexName}/mapping`);
}

export const getSetting = (clusterPhyName: string, indexName: string) => {
  return fetch(`/v3/indices/${clusterPhyName}/${indexName}/setting`);
}

export const getIndexDetail = (clusterPhyName: string, indexName: string) => {
  return fetch(`/v3/indices/select?clusterPhyName=${clusterPhyName}&indexName=${indexName}`);
}