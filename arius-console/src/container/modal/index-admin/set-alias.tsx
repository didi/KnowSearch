import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { setAlias } from "api/index-admin";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const SetAlias = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "alias",
        label: "别名",
        type: FormItemType.input,
        attrs: {
          placeholder: "请输入别名，多个用','分割",
        },
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
        rules: [
          {
            required: true,
            message: "请输入别名，1-30字符",
            validator: (rule: any, value: any) => {
              value = value?.trim();
              if (value === undefined || !value.length || value.length > 30) {
                return Promise.reject();
              } else {
                return Promise.resolve();
              }
            },
          },
        ],
      },
      {
        key: "aliases",
        label: "已有别名",
        type: FormItemType.custom,
        customFormItem: (
          <div className="btn-labels-box">
            {props.params?.aliases?.length ? props.params?.aliases?.map((item) => <div className="btn-labels">{item}</div>) : "-"}
          </div>
        ),
      },
    ] as IFormItem[],
    visible: true,
    title: "设置别名",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const params = {
        cluster: props.params.cluster,
        index: props.params.index,
        aliases: result.alias.split(","),
      };
      return setAlias(params).then(() => {
        message.success(`设置别名成功`);
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
