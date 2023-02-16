import React, { useEffect, useState } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { ExpectDataNodeNu, RenderText, ShowCost } from "container/custom-form";
import { IWorkOrder } from "typesPath/params-types";
import { getDiskSize } from "api/cluster-api";
import { submitWorkOrder } from "api/common-api";
import { regNonnegativeInteger } from "constants/reg";
import { ICluster } from "typesPath/cluster/cluster-types";
import { AppState, UserState } from "store/type";
import { bytesUnitFormatter } from "lib/utils";
import { getRegionNodeSpec } from "api/op-cluster-region-api";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ExpandShrinkModal = (props: { dispatch: any; cb: Function; params: any; app: AppState; user: UserState }) => {
  const [count, setCount] = useState(props.params?.dataNodeNum || 0);
  const [diskSize, setDiskSize] = useState("" as string | number);
  const [dataNodeSpec, setDataNodeSpec] = useState("-");
  useEffect(() => {
    _getDiskSize();
  }, [count]);

  const _getDiskSize = async () => {
    let res = await getDiskSize(props.params.id, count);
    let size = res === -1 ? "-" : bytesUnitFormatter(res);
    setDiskSize(size);
  };

  useEffect(() => {
    getRegionNodeSpec(props.params.id).then((res) => {
      setDataNodeSpec(res || "-");
    });
  }, []);

  const xFormModalConfig = {
    formMap: [
      {
        key: "name",
        label: "集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.name} />,
      },
      {
        key: "oldDataNodeNu",
        label: "现有节点数",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params?.dataNodeNum || 0} />,
      },
      {
        key: "dataNodeNum",
        label: "期望节点数",
        type: FormItemType.custom,
        isCustomStyle: true,
        CustomStyle: { marginBottom: 0 },
        customFormItem: <ExpectDataNodeNu min={0} podNumber={props.params?.dataNodeNum || 0} />,
        rules: [
          {
            required: true,
            validator: (rule: any, value: any) => {
              if (props.params?.dataNodeNum === value) {
                return Promise.reject("不能与原来一样");
              }
              if (value < 1) {
                return Promise.reject("请输入节点个数，大于等于1的正整数");
              } else {
                setCount(value);
                return Promise.resolve();
              }
            },
          },
        ],
      },
      {
        key: "dataNodeSpec",
        label: "节点规格",
        type: FormItemType.text,
        isCustomStyle: true,
        CustomStyle: { marginTop: 16 },
        customFormItem: <RenderText text={dataNodeSpec || "-"} />,
      },
      {
        key: "diskSize",
        label: "磁盘大小预估",
        type: FormItemType.text,
        isCustomStyle: true,
        CustomStyle: { marginTop: 16 },
        customFormItem: <RenderText text={diskSize || "-"} />,
      },
      // {
      //   key: 'clusterCost',
      //   label: '集群成本',
      //   type: FormItemType.text,
      //   customFormItem: <ShowCost />,
      // },
      {
        key: "description",
        label: "申请原因",
        type: FormItemType.textArea,
        rules: [
          {
            required: true,
            whitespace: true,
            validator: (rule: any, value: string) => {
              if (!value || value?.trim().length >= 100) {
                return Promise.reject("请输入1-100字申请原因");
              } else {
                return Promise.resolve();
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
    title: "集群扩缩容",
    formData: props.params || {},
    needBtnLoading: true,
    width: 660,
    okText: "提交",
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      const params: IWorkOrder = {
        contentObj: {
          logicClusterId: props.params.id,
          logicClusterName: props.params.name,
          dataNodeSpec,
          dataNodeNu: result.dataNodeNum,
          memo: result.memo,
        },
        submitorProjectId: props.app.appInfo()?.id,
        submitor: props.user.getName("userName"),
        description: result.description,
        type: "logicClusterIndecrease",
      };
      await submitWorkOrder(params, props.params?.history, () => {
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

export default connect(mapStateToProps)(ExpandShrinkModal);
