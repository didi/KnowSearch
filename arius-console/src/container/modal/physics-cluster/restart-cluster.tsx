import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { OrderNode, RenderText } from "container/custom-form";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { PHY_CLUSTER_TYPE } from "constants/status-map";
import { IWorkOrder } from "typesPath/params-types";
import { submitWorkOrder } from "api/common-api";
import { AppState, UserState } from "store/type";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
  app: state.app,
});

const RestartClusterModal = (props: {
  dispatch: any;
  cb: Function;
  app: AppState;
  user: UserState;
  params: IOpPhysicsClusterDetail;
}) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "name",
        label: "集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.cluster} />,
      },
      {
        key: "type",
        label: "集群类型",
        type: FormItemType.text,
        customFormItem: (
          <RenderText
            text={
              PHY_CLUSTER_TYPE.find((row) => row.value === props.params.type)
                ?.label || ""
            }
          />
        ),
      },
      {
        key: "user",
        label: "申请人",
        type: FormItemType.text,
        customFormItem: (
          <RenderText text={props.user.getName("domainAccount")} />
        ),
      },
      {
        key: "roleOrder",
        label: "重启节点顺序",
        type: FormItemType.custom,
        customFormItem: <OrderNode id={props.params.id} />,
      },
      {
        key: "description",
        type: FormItemType.textArea,
        label: "申请原因",
        rules: [
          {
            required: true,
            whitespace: true,
            validator: (rule: any, value: string) => {
              if (value?.trim().length > 0 && value?.trim().length <= 100) {
                return Promise.resolve();
              } else if (value?.trim().length > 100) {
                return Promise.reject('请输入1-100字申请原因');
              } else {
                return Promise.reject('请输入1-100字申请原因');
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入1-100字申请原因",
        },
      },
    ] as IFormItem[],
    visible: true,
    title: "集群重启",
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      result.roleOrder = result.roleOrder.map((item) => item.roleClusterName);
      const params: IWorkOrder = {
        contentObj: {
          phyClusterId: props.params.id,
          phyClusterName: props.params.cluster,
          roleOrder: result.roleOrder,
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName("domainAccount"),
        description: result.description || "",
        type: "clusterOpRestart",
      };
      submitWorkOrder(params, () => {
        props.dispatch(actions.setModalId(""));
      });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(RestartClusterModal);
