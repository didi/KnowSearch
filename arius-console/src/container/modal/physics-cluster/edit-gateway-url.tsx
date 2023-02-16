import React, { useState } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { AppState, UserState } from "store/type";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { bindGateway } from "api/cluster-api";
import { regIp, regPort } from "constants/reg";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const EditGatewayUrl = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "gatewayUrl",
        label: "Gateway地址",
        type: FormItemType.input,
        attrs: {
          placeholder: "请输入Gateway地址",
        },
        isCustomStyle: true,
        CustomStyle: { margin: 0 },
        rules: [
          {
            required: true,
            validator: (_, value: string) => {
              if (!value) {
                return Promise.reject("请输入Gateway地址 ，不同地址用逗号分隔。");
              }
              return Promise.resolve();
            },
          },
        ],
      },
    ] as IFormItem[],
    visible: true,
    title: "编辑Gateway地址",
    formData: { gatewayUrl: props.params.gatewayUrl },
    isWaitting: true,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      let { gatewayUrl } = result;
      while (gatewayUrl.includes("，")) {
        gatewayUrl = gatewayUrl.replace("，", ",");
      }
      let params = {
        id: props.params.id,
        gatewayUrl,
      };
      await bindGateway(params);
      props.dispatch(actions.setModalId(""));
      props.cb();
    },
    width: 480,
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(EditGatewayUrl);
