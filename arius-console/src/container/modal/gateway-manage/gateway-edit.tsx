import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import store from "store";
import { message } from "antd";
import { RenderText } from "container/custom-form";
import { updateGateway } from "api/gateway-manage";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const GatewayEdit = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;

  const xFormModalConfig = {
    formMap: [
      {
        key: "gatewayClusterName",
        label: "Gateway集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.clusterName} />,
      },
      {
        key: "memo",
        label: "描述",
        type: FormItemType.textArea,
      },
    ] as IFormItem[],
    formData: params,
    visible: true,
    title: `编辑Gateway`,
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const data = {
        id: params.id,
        memo: result.memo,
      };
      return updateGateway(params.id, data).then(() => {
        message.success(`操作成功`);
        dispatch(actions.setModalId(""));
        cb && cb();
      });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
