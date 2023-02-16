import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { OrderNode, RenderText } from "container/custom-form";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { AppState, UserState } from "store/type";
import { clusterRestart } from "api/cluster-api";
import { showSubmitTaskSuccessModal } from "container/custom-component";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  user: state.user,
  app: state.app,
});

const RestartClusterModal = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: IOpPhysicsClusterDetail }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "name",
        label: "集群名称",
        type: FormItemType.text,
        isCustomStyle: true,
        CustomStyle: { marginTop: 24 },
        customFormItem: <RenderText text={props.params.cluster} />,
      },
      {
        key: "roleOrder",
        label: "重启节点顺序",
        type: FormItemType.custom,
        customFormItem: <OrderNode id={props.params.id} />,
      },
    ] as IFormItem[],
    visible: true,
    title: "集群重启",
    formData: props.params || {},
    isWaitting: true,
    type: "drawer",
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onSubmit: async (result: any) => {
      result.roleOrder = result.roleOrder.map((item) => item.roleClusterName);
      let params = {
        phyClusterId: props.params.id,
        phyClusterName: props.params.cluster,
        roleOrder: result.roleOrder,
      };
      let expandData = JSON.stringify(params);
      let ret = await clusterRestart({ expandData });
      props.dispatch(actions.setDrawerId(""));
      showSubmitTaskSuccessModal(ret, props.params?.history);
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(RestartClusterModal);
