import fetch from "../lib/fetch";

// 获取dcdr主从切换任务详情
export const getDcdrDetail = (taskId: number) => {
  return fetch(`/v3/dcdr/work-order/task/${taskId}/detail`);
};

// 获取模板dcdr主从切换任务详情
export const getTemplateDcdrDetail = (taskId: number, templateId: number) => {
  return fetch(`/v3/dcdr/work-order/task/${taskId}/${templateId}/detail`);
};

// 根据任务id取消全量DCDR主从切换任务
export const cancelDcdr = (taskId: number) => {
  return fetch(`/v3/dcdr/work-order/task/${taskId}/cancel`, {
    method: "DELETE",
  });
};

// 根据任务id和模板id取消单个DCDR主从切换任务
export const canceltemplateDcdr = (taskId: number, templateIds: number) => {
  return fetch(`/v3/dcdr/work-order/task/${taskId}/${templateIds}/cancel`, {
    method: "DELETE",
  });
};

// dcdr主从强制切换接口
export const dcdrForceSwitch = (taskId: number, templateId: number) => {
  return fetch(`/v3/dcdr/work-order/task/${taskId}/${templateId}/forceSwitch`, {
    method: "PUT",
  });
};

// 刷新dcdr链路任务
export const dcdrRefresh = (taskId: number, templateId: number) => {
  return fetch(`/v3/dcdr/work-order/task/${taskId}/${templateId}/refresh`, {
    method: "POST",
  });
};
