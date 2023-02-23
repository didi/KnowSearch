import fetch from "../lib/fetch";

// 数据迁移提交迁移任务接口
export const queryFastIndex = (params: any) => {
  return fetch(`/v3/op-task/fast-index`, {
    method: "POST",
    body: params,
  });
};

// 获取任务详情
export const getFastIndexDetail = (taskId: number) => {
  return fetch(`/v3/op-task/fast-index/${taskId}/detail`);
};

// 查询任务日志
export const getFastIndexLog = (params: any) => {
  return fetch(`/v3/op-task/fast-index/logs`, {
    method: "POST",
    body: params,
  });
};

// 根据任务id修改限流值
export const updateRateLimit = (taskId: number, params: { taskReadRate: number }) => {
  return fetch(`/v3/op-task/fast-index/${taskId}/rate-limit`, {
    method: "PUT",
    body: params,
  });
};

// 根据任务id取消任务
export const cancelFastIndexTask = (taskId: number) => {
  return fetch(`/v3/op-task/fast-index/${taskId}`, {
    method: "DELETE",
  });
};

// 根据任务id重试任务
export const retryFastIndexTask = (taskId: number) => {
  return fetch(`/v3/op-task/fast-index/${taskId}`, {
    method: "PUT",
  });
};

// 根据任务id回切转让
export const rollbackFastIndexTask = (taskId: number) => {
  return fetch(`/v3/op-task/fast-index/${taskId}/rollback`, {
    method: "PUT",
  });
};

// 获取索引模板&索引
export const getFastIndexBrief = (taskId: number) => {
  return fetch(`/v3/op-task/fast-index/${taskId}/brief`);
};

// 索引模板 目标集群的setting
export const getTemplateSetting = (logicClusterId: number, logicId: number) => {
  return fetch(`/v3/op-task/fast-index/template/${logicClusterId}/${logicId}/setting`);
};

// 索引 目标集群的setting
export const getIndexSetting = (cluster: string, indexName: string) => {
  return fetch(`/v3/op-task/fast-index/${cluster}/${indexName}/setting`);
};
