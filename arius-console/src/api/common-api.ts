import fetch from "../lib/fetch";
import { apiCache } from "lib/api-cache";
import { showSubmitOrderSuccessModal } from "container/custom-component";
import { IWorkOrder } from "typesPath/params-types";

/*
 * 外部的接口
 *@部门、用户
 */

export const getAllUserList = () => {
  return fetch("/v3/security/project/bind-user");
};

// 工单接口
export const submitWorkOrder = (params: any, history?: any, actionAfterSubmit?: () => any, modalWidth?: number) => {
  return fetch(`/v3/order/${params.type}/submit`, {
    method: "PUT",
    body: JSON.stringify(params),
  }).then((data) => {
    actionAfterSubmit();
    showSubmitOrderSuccessModal(
      {
        title: data?.title || "",
        id: data.id,
        width: modalWidth,
      },
      history
    );
    return data;
  });
};
