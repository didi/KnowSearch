import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType } from "component/x-form";
import { notification } from "antd";
import { getLoginClusterPhysicsClusterList } from "api/cluster-api";
import { AppState, RegionState } from "store/type";
import {
  LogRegionSelect,
  PhyCluster,
  RegionRacksText,
} from "container/custom-form/region";
import { logicClusterBinRegion } from "api/op-cluster-region-api";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  region: state.region,
  app: state.app,
});

const RelationRegionModal = (props: {
  dispatch: any;
  cb: Function;
  region: RegionState;
  params: any;
  app: AppState;
}) => {
  React.useEffect(() => {
    const { id } = props.params;
    if (!id) return;
    getLoginClusterPhysicsClusterList(id).then((res) => {
      if (res) {
        res = res.map((item) => {
          return {
            value: item,
            label: item,
          };
        });
        props.dispatch(actions.setPhyClusterList(res));
      }
    });
  }, []);

  const xFormModalConfig = {
    formMap: [
      {
        key: "clusterName",
        label: "物理集群",
        type: FormItemType.custom,
        customFormItem: <PhyCluster />,
        rules: [
          {
            required: true,
            message: "请选择集群",
          },
        ],
      },
      {
        key: "regionId",
        label: "region",
        type: FormItemType.custom,
        customFormItem: <LogRegionSelect />,
        rules: [
          {
            required: true,
            message: "请选择region",
          },
        ],
      },
      {
        key: "racks",
        label: "racks",
        type: FormItemType.text,
        customFormItem: <RegionRacksText />,
      },
    ],
    visible: true,
    title: "关联region",
    formData: {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
      props.dispatch(actions.setRacks(""));
      props.dispatch(actions.setRegionList([]));
    },
    onSubmit: (result: any) => {
      result.responsible = Array.isArray(result.responsible)
        ? result.responsible.join(",")
        : result.responsible;
      result.appId = props.app.appInfo()?.id;
      result.dataCenter = "cn";
      result.dataNodeNu = 0;
      result.dataNodeSpec = "";
      result.configJson = "";
      result.id = props.params.id;
      result.libraDepartment = "";
      result.libraDepartmentId = "";
      result.quota = 0;
      result.type = props.params.type;
      const clusterRegionDTOS = result.regionId?.map((item) => ({
        id: item,
        phyClusterName: result.clusterName,
      }));
      result.clusterRegionDTOS = clusterRegionDTOS;
      logicClusterBinRegion(result)
        .then((res) => {
          notification.success({ message: `关联成功` });
          props.dispatch(actions.setModalId(""));
          props.dispatch(actions.setRacks("")); // 清空仓库
          props.dispatch(actions.setRegionList([]));
        })
        .finally(() => {
          props.cb && props.cb(); // 重新获取数据列表
        });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(RelationRegionModal);
