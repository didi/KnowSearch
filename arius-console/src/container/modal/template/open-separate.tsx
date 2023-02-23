import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { regNonnegativeInteger } from "constants/reg";
import { updateTemplateSrv } from "api/cluster-index-api";
import { CodeType } from "constants/common";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const OpenSeparate = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const { id, hotTime, expireTime } = props.params;
  const xFormModalConfig = {
    formMap: [
      {
        key: "tips",
        label: "",
        type: FormItemType.custom,
        customFormItem: <>确定开启冷热分离能力？请完善热节点保存天数</>,
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
      },
      {
        key: "coldSaveDays",
        label: "热节点保存天数",
        type: FormItemType.inputNumber,
        attrs: {
          placeholder: "请输入热节点保存天数",
          style: {
            width: "150px",
          },
        },
        rules: [
          {
            required: true,
            message: `请输入热节点保存天数（${expireTime !== -1 ? "1-" + expireTime : "1或以上"}的正整数）`,
            validator: (rule: any, value: any) => {
              if (expireTime !== -1) {
                if (new RegExp(regNonnegativeInteger).test(value) && value > 0 && value <= expireTime) return Promise.resolve();
              } else {
                if (new RegExp(regNonnegativeInteger).test(value) && value > 0) return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
      },
    ] as IFormItem[],
    formData: {
      coldSaveDays: hotTime !== -1 ? hotTime : undefined,
    },
    visible: true,
    title: "提示",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const params = {
        srvCode: CodeType.Separate,
        templateIdList: [id],
        params: {
          coldSaveDays: result.coldSaveDays,
        },
      };
      return updateTemplateSrv(params).then(() => {
        message.success(`操作成功`);
        props.dispatch(actions.setModalId(""));
        props.cb && props.cb(); // 重新获取数据列表
      });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
