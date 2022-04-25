import fetch from '../lib/fetch';

/**
 * order 相关接口
 *
 */

export const getTypeEnums = () => {
  return fetch(`/v3/normal/order/type-enums`);
};

export const getApplyOrderList = (status: number) => {
  return fetch(`/v3/normal/order/orders?status=${status === 1 ? '' : status}`);
};

export const getApprovalOrderList = (status: number) => {
  return fetch(`/v3/normal/order/approvals?status=${status === 1 ? '' : status}`);
};


export const cancelOrder = (orderId: number) => {
  return fetch(`/v3/normal/order/${orderId}`, {
    method: 'DELETE',
  });
};

export const getOrderDetail = (orderId: number) => {
  return fetch(`/v3/normal/order/${orderId}`);
};

export const approvalOrder = (params: any) => {
  return fetch(`/v3/normal/order/${params.orderId}`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
};
