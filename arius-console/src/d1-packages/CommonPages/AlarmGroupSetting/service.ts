import { request, post, put, delete as deleteMethod } from "../../Utils/request";

export const queryAlarmSettingList = (params): any => {
  return post(`/v3/normal/monitor/notifyGroups`, params);
};

export const queryAlarmSettingDetail = (id): any => {
  return request(`/v3/normal/monitor/notifyGroup/${id}`);
};

export const createOrUpdateAlarmSetting = (isCreate: boolean, params): any => {
  const reqMethod = isCreate ? post : put;
  return reqMethod("/v3/normal/monitor/notifyGroup", params);
};

export const deleteAlarmSetting = (id: number): any => {
  return deleteMethod(`/v3/normal/monitor/notifyGroup/${id}`);
};

export const switchAlarmSettingStatus = (id: number, status: number): any => {
  return post(`/v3/normal/monitor/notifyGroup/switch/${id}?status=${status}`);
};

export const queryAlarmSettingStatus = (id: number): any => {
  return request(`/v3/normal/monitor/notifyGroup/${id}/inuse`);
};

export const queryUserList = (keyword = "root"): any => {
  return request(`/v3/normal/monitor/notifyGroup/users?keyword=${keyword}`);
};
