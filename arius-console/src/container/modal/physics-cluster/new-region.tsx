import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { RenderText } from "container/custom-form";
import { RacksTransfer } from "container/custom-form/racks-transfer";
import { clusterRegionEdit, clusterRegionNew } from "api/op-cluster-region-api";
import { notification } from "antd";
import { INodeDivide } from "typesPath/index-types";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const NewRegionModal = (props: {
  dispatch: any;
  cb: Function;
  params: {
    clusterName: string;
    nodeDivideList: INodeDivide[];
    record?: INodeDivide;
  };
}) => {
  const xFormModalConfig = {
    formMap: [
      // {
      //   key: 'name',
      //   label: 'region ID',
      //   type: FormItemType.text,
      //   customFormItem: <RenderText text={props.params.record?.regionId} />,
      // },
      {
        key: "racks",
        label: "racks",
        type: FormItemType.custom,
        customFormItem: (
          <RacksTransfer
            nodeDivideList={props.params.nodeDivideList}
            clusterName={props.params.clusterName}
            editData={props.params.record}
          />
        ),
        rules: [
          {
            required: true,
            message: "请选择racks, 大于1",
            validator: (rule: any, value: string) => {
              if (value && value.length >= 1) {
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
      },
      {
        key: "configJson",
        label: "配置",
        type: FormItemType.textArea,
        rules: [
          {
            required: false,
          },
        ],
        attrs: {
          placeholder: "{},json格式",
        },
      },
    ] as IFormItem[],
    formData: props.params.record ? props.params.record : {},
    visible: true,
    width: 800,
    title: props.params.record ? "编辑Region" : "新增Region",
    isWaitting: true,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (value: any) => {
      value.logicClusterId = 0;
      value.clusterName = props.params.clusterName;
      value.racks = Array.isArray(value.racks)
        ? value.racks.join(",")
        : value.racks;
      if (props.params.record?.regionId) {
        value.regionId = props.params.record.regionId;
        return clusterRegionEdit(value)
          .then(() => {
            notification.success({ message: "编辑成功" });
            props.cb();
          })
          .finally(() => {
            props.dispatch(actions.setModalId(""));
          });
      } else {
        return clusterRegionNew(value)
          .then(() => {
            notification.success({ message: "新建成功" });
            props.cb();
          })
          .finally(() => {
            props.dispatch(actions.setModalId(""));
          });
      }
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(NewRegionModal);
