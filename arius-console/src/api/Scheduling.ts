import fetch from '../lib/fetch';

interface ILogsList {
  taskId?: number,
  page: number,
  size: number,
  taskName?: string,
  taskStatus?: number,
  beginTime?: number,
  endTime?: number
}

interface ITastList {
  taskId?: number,
  page: number,
  size: number,
  taskStatus?: number,
  taskDesc?: string,
  className?: string,
}

export const getTaskList = (params: ITastList) => {
  return fetch(`/v1/logi-job/task/list`, {
    method: 'POST',
    body: params,
  });
};

export const taskDo = (taskCode: number) => {
  return fetch(`/v1/logi-job/task/${taskCode}/do`, {
    method: 'POST',
    body: {},
  });
};

export const taskStatus = (taskCode: number, status: number) => {
  return fetch(`/v1/logi-job/task/${taskCode}/${status}`, {
    method: 'POST',
    body: {},
  });
};

export const jobStop = (jobCode: number) => {
  return fetch(`/v1/logi-job/job/${jobCode}/stop`, {
    method: 'POST',
    body: {},
  });
};

export const getLogsList = (params: ILogsList) => {
  return fetch(`/v1/logi-job/logs/list`, {
    method: 'POST',
    body: params,
  });
};

export const upgradeTask = (taskCode: number) => {
  return fetch(`/v1/logi-job/task/${taskCode}/detail`);
};
