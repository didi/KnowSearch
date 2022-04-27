import fetch, { formFetch } from '../lib/fetch';

export const getTaskList = () => {
  return fetch(`/v3/op/worktask/tasks`);
};

export const getTaskBaseInfo = (id: number) => {
  return fetch(`/v3/op/ecm/work-order/task/${id}/basic-info`);
};

export const getTaskDetail = (id: number) => {
  return fetch(`/v3/op/ecm/work-order/task/${id}/task-details`);
};

export const getTaskLog = (id: number) => {
  return fetch(`/v3/op/ecm/work-order/task/${id}/task-detail/log`);
};

export const redofailedTask = (id: number, hostname: string) => { // 重试
  return fetch(`/v3/op/ecm/work-order/task/${id}/redo-failed?hostname=${hostname}`, {
    method: 'POST',
  });
};

export const skipfailedTask = (id: number, hostname: string) => { // 跳过
  return fetch(`/v3/op/ecm/work-order/task/${id}/skip-failed?hostname=${hostname}`, {
    method: 'POST',
  });
};

export const pauseTask = (id: number) => { // 暂停
  return fetch(`/v3/op/ecm/work-order/task/${id}/pause`, {
    method: 'POST',
  });
};

export const continueTask = (id: number) => {  // 继续
  return fetch(`/v3/op/ecm/work-order/task/${id}/continue`, {
    method: 'POST',
  });
};

export const cancalTask = (id: number) => { // 取消
  return fetch(`/v3/op/ecm/work-order/task/${id}/cancel`, {
    method: 'POST',
  });
};

export const createTask = (id: number) => { // 启动
  return fetch(`/v3/op/ecm/work-order/task/${id}/create`, {
    method: 'POST',
  });
};

export const scaleTask = (id: number) => { // 扩缩容
  return fetch(`/v3/op/ecm/work-order/task/${id}/scale`, {
    method: 'POST',
  });
};

export const restartTask = (id: number) => { // 重启
  return fetch(`/v3/op/ecm/work-order/task/${id}/restart`, {
    method: 'POST',
  });
};
export const retryTask = (id: number) => { // 重试
  return fetch(`/v3/op/ecm/work-order/task/${id}/retry`, {
    method: 'POST',
  });
};

export const upgradeTask = (id: number) => { // 升级
  return fetch(`/v3/op/ecm/work-order/task/${id}/upgrade`, {
    method: 'POST',
  });
};
