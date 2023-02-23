import fetch from "../lib/fetch";

interface ILogsList {
  taskId?: number;
  page: number;
  size: number;
  taskName?: string;
  taskStatus?: number;
  beginTime?: number;
  endTime?: number;
}

interface ITastList {
  taskId?: number;
  page: number;
  size: number;
  taskStatus?: number;
  taskDesc?: string;
  className?: string;
}

// 获取任务列表接口
export const getTaskList = (params: ITastList) => {
  return fetch(`/v1/logi-job/task/list`, {
    method: "POST",
    body: params,
  });
};

// 执行任务
export const taskDo = (taskCode: number) => {
  return fetch(`/v1/logi-job/task/${taskCode}/do`, {
    method: "POST",
    body: {},
  });
};

// 暂停/恢复任务
export const taskStatus = (taskCode: number, status: number) => {
  return fetch(`/v1/logi-job/task/${taskCode}/${status}`, {
    method: "POST",
    body: {},
  });
};

// 终止任务
export const jobStop = (jobCode: number) => {
  return fetch(`/v1/logi-job/job/${jobCode}/stop`, {
    method: "POST",
    body: {},
  });
};

// 获取调度日志列表
export const getLogsList = (params: ILogsList) => {
  return fetch(`/v1/logi-job/logs/list`, {
    method: "POST",
    body: params,
  });
};

export const upgradeTask = (taskCode: number) => {
  return fetch(`/v1/logi-job/task/${taskCode}/detail`);
};

// 编辑调度任务
export const updateTask = (taskCode: number, params) => {
  return fetch(`/v1/logi-job/task/${taskCode}/update`, {
    method: "POST",
    body: params,
  });
};

// 复制调度任务
export const copyTask = (taskCode: number, params) => {
  return fetch(`/v1/logi-job/task/${taskCode}/copy`, {
    method: "POST",
    body: params,
  });
};

// 删除任务
export const deleteTask = (taskCode: number) => {
  return fetch(`/v1/logi-job/task/${taskCode}`, {
    method: "DELETE",
  });
};

// 获取所有的调度器地址
export const getWorkerList = () => {
  return fetch(`/v1/logi-job/worker/list`);
};
