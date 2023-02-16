import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { deleteAlias } from "api/index-admin";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const DeleteAlias = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "alias",
        label: "请选择需要删除的别名",
        type: FormItemType.checkBox,
        options: props.params.aliases || [],
        rules: [
          {
            required: true,
            message: "请选择需要删除的别名",
            validator: (rule: any, value: any) => {
              if (value === undefined || !value.length) {
                return Promise.reject();
              } else {
                return Promise.resolve();
              }
            },
          },
        ],
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
      },
    ] as IFormItem[],
    visible: true,
    title: "删除别名",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const params = {
        cluster: props.params.cluster,
        index: props.params.index,
        aliases: result.alias,
      };
      return deleteAlias(params).then(() => {
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
