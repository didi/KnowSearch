import fetch from "../lib/fetch";

/**
 * order 相关接口
 *
 */

// 获取工单类型
export const getTypeEnums = () => {
  return fetch(`/v3/order/type-enums`);
};

export const getApplyOrderList = (status: number) => {
  return fetch(`/v3/order/orders?status=${status === 1 ? "" : status}`);
};

export const getApprovalOrderList = (status: number) => {
  return fetch(`/v3/order/approvals?status=${status === 1 ? "" : status}`);
};

export const cancelOrder = (orderId: number) => {
  return fetch(`/v3/order/${orderId}`, {
    method: "DELETE",
  });
};

// 工单详情
export const getOrderDetail = (orderId: number) => {
  return fetch(`/v3/order/${orderId}`);
};

export const approvalOrder = (params: any) => {
  return fetch(`/v3/order/${params.orderId}`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};
