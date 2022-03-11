
import fetch from '../lib/fetch';
import { apiCache } from 'lib/api-cache';
import { showSubmitOrderSuccessModal } from 'container/custom-component';
import { IWorkOrder } from 'typesPath/params-types';

/*
 * 外部的接口
 *@部门、用户
 */

export const getStaff = (keyword: string) => {
  const api = `/v3/normal/account/search?keyWord=${keyword}`;
  const cacheData = apiCache.getDataFromCache(api);
  if (cacheData) {
    return new Promise(res => res(cacheData));
  }
  return fetch(api).then((data) => {
    apiCache.setCacheMap(api, data);
    return data;
  }).catch(() => {
    apiCache.deleteDataFromCache(api);
    return [];
  });
};

export const getAllUser = () => {
  return fetch('/v3/normal/user/list');
}

// 工单接口
export const submitWorkOrder = (params: IWorkOrder, actionAfterSubmit?: () => any) => {
  return fetch(`/v3/normal/order/${params.type}/submit`, {
    method: 'PUT',
    body: JSON.stringify(params),
  }).then((data) => {
    showSubmitOrderSuccessModal({
      title: data?.title || '',
      id: data.id,
      onOk: actionAfterSubmit,
    });
    return data;
  });
};

