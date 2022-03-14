import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { RenderText } from "container/custom-form";
import { DockerExpectDataNodeNu } from "container/custom-form";
import { VERSION_MAINFEST_TYPE } from "constants/status-map";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { IWorkOrder } from "typesPath/params-types";
import { submitWorkOrder } from "api/common-api";
import { AppState, UserState } from "store/type";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const DockerExpandShrinkCluster = (props: {
  dispatch: any;
  cb: Function;
  app: AppState;
  user: UserState;
  params: IOpPhysicsClusterDetail;
}) => {
  const dataData = props.params?.esRoleClusterVOS?.filter(
    (ele) => ele.role === "datanode"
  );
  const masternode = props.params?.esRoleClusterVOS?.filter(
    (ele) => ele.role === "masternode"
  );
  const clientnode = props.params?.esRoleClusterVOS?.filter(
    (ele) => ele.role === "clientnode"
  );
  const dataObj = dataData[0];

  const xFormModalConfig = {
    formMap: [
      {
        key: "cluster",
        label: "集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.cluster} />,
      },
      {
        key: "project",
        label: "所属项目",
        type: FormItemType.text,
        customFormItem: <RenderText text={"es"} />,
      },
      {
        key: "type",
        label: "集群类型",
        type: FormItemType.text,
        customFormItem: (
          <RenderText text={VERSION_MAINFEST_TYPE[props.params.type]} />
        ),
      },
      {
        key: "esVersion",
        label: "ES版本",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.esVersion} />,
      },
      {
        key: "machineSpec",
        label: "Datanode规格",
        type: FormItemType.text,
        customFormItem: <RenderText text={dataObj.machineSpec} />,
      },
      {
        key: "dataNodeNu",
        label: "现有Datanode节点数",
        type: FormItemType.text,
        customFormItem: <RenderText text={dataObj.podNumber} />,
      },
      {
        key: "podNumber",
        label: "期望Datanode节点数",
        type: FormItemType.custom,
        customFormItem: (
          <DockerExpectDataNodeNu
            podNumber={dataObj.podNumber}
            style={{ width: "60%" }}
          />
        ),
        rules: [
          {
            required: true,
            validator: (rule: any, value: number) => {
              if (dataObj.podNumber === value) {
                return Promise.reject("不能与原来一样");
              }
              if (value && value > 1) {
                return Promise.resolve();
              }
              return Promise.reject("大于1");
            },
          },
        ],
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
              if (value?.trim().length > 0 && value?.trim().length < 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入1-100个字符");
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入该项目描述，0-50字",
        },
      },
    ] as IFormItem[],
    visible: true,
    title: "扩缩容",
    formData: dataObj || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const roleClusters = [];
      if (dataObj?.podNumber !== result?.dataNodeNu) {
        const datanodeObj = {
          role: "datanode",
          originPodNumber: dataObj?.podNumber,
          podNumber: result?.podNumber,
          pidCount: "",
          machineSpec: dataObj?.machineSpec,
        };
        const masternodeObj = {
          role: "masternode",
          originPodNumber: masternode[0]?.podNumber,
          podNumber: masternode[0]?.podNumber,
          pidCount: "",
          machineSpec: masternode[0]?.machineSpec,
        };
        const clientnodeObj = {
          role: "clientnode",
          originPodNumber: clientnode[0]?.podNumber,
          podNumber: clientnode[0]?.podNumber,
          pidCount: "",
          machineSpec: clientnode[0]?.machineSpec,
        };
        roleClusters.push(datanodeObj, masternodeObj, clientnodeObj);
      }
      const contentObj = {
        type: 3,
        operationType: dataObj.podNumber - result?.podNumber >= 0 ? 3 : 2, //  2:扩容 3:缩容
        phyClusterId: props.params.id,
        phyClusterName: props.params.cluster,
        roleClusters,
      };
      const params: IWorkOrder = {
        contentObj,
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description || "",
        type: "clusterOpIndecrease",
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

export default connect(mapStateToProps)(DockerExpandShrinkCluster);
