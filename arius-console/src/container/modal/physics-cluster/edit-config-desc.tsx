import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { AppState } from "store/type";
import { updateCongig } from "api/op-cluster-config-api";
import { notification } from "antd";
import { IPhyConfig } from "typesPath/cluster/physics-type";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const EditConfigDesc = (props: {
  dispatch?: any;
  cb?: Function;
  app?: AppState;
  user?: any;
  params?: IPhyConfig;
}) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "desc",
        type: FormItemType.textArea,
        label: "描述信息",
        rules: [
          {
            validator: (rule: any, value: string) => {
              if (props.params?.configData === value) {
                return Promise.reject("不可与原来一致。");
              }
              return Promise.resolve();
            },
          },
        ],
        attrs: {
          placeholder: "请输入描述信息，0-100字",
        },
      },
    ] as IFormItem[],
    visible: true,
    title: "编辑描述信息",
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const req = {
        clusterId: props.params.clusterId,
        desc: result.desc,
        enginName: props.params?.enginName,
        typeName: props.params?.typeName,
        id: props.params.id,
      };
      return updateCongig(req).then(() => {
        notification.success({ message: "编辑配置信息成功！" });
        props.dispatch(actions.setModalId(""));
        props.cb();
      });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(EditConfigDesc);
