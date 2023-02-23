import fetch, { formFetch } from "../lib/fetch";

export const getTaskList = (params) => {
  return fetch(`/v3/op-task/page`, {
    method: "POST",
    body: params,
  });
};

// 任务详情
export const getTaskBaseInfo = (taskId: number) => {
  return fetch(`/v3/op-task/${taskId}`);
};

// 获取任务类型
export const getTaskType = () => {
  return fetch(`/v3/op-task/type-enums`);
};

// 执行进度接口
export const getTaskDetail = (taskId: number) => {
  return fetch(`/v3/ecm/op-task/detail-info?taskId=${taskId}`);
};

// 对主任务进行相应的操作
// action: start、pause、kill、cancel、un_know
export const actionTask = (action: string, taskId: number) => {
  return fetch(`/v3/ecm/op-task/${action}/${taskId}`, {
    method: "POST",
  });
};

// 对任务子节点进行相应的操作
// action: ignore redo kill
export const actionHostTask = (params) => {
  return fetch(`/v3/ecm/op-task/${params.action}/${params.taskId}/${params.host}?groupName=${params?.groupName}`, {
    method: "POST",
  });
};

// 执行任务
export const executeTask = (taskId: number) => {
  return fetch(`/v3/ecm/op-task/execute/${taskId}`, {
    method: "POST",
  });
};

// 重试任务
export const retryTask = (taskId: number) => {
  return fetch(`/v3/ecm/op-task/retry/${taskId}`, {
    method: "POST",
  });
};

// 回滚
export const rollbackTask = (id: number) => {
  return fetch(`/v3/op/ecm/work-order/task/${id}/rollback`, {
    method: "POST",
  });
};

// zeus 查看任务执行完成后的错误输出
export const getStderrLog = (taskId: number, hostname: string, groupName: string) => {
  return fetch(`/v3/ecm/op-task/log/stderr?taskId=${taskId}&hostname=${hostname}&groupName=${groupName}`);
};

// zeus 查看任务执行完成后的标准输出
export const getStdoutLog = (taskId: number, hostname: string, groupName: string) => {
  return fetch(`/v3/ecm/op-task/log/stdout?taskId=${taskId}&hostname=${hostname}&groupName=${groupName}`);
};

// 集群配置回滚
export const rollbackClusterConfig = (params) => {
  return fetch(`/v3/op-task/es-cluster-config-rollback`, {
    method: "POST",
    body: params,
  });
};
