import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { regClusterName, regNonnegativeInteger } from "constants/reg";
import { StaffSelect } from "container/staff-select";
import { opAddLogicCluster } from "api/cluster-api";
import { DataNode, RenderText } from "container/custom-form";
import { AppState, UserState } from "store/type";
import { IWorkOrder } from "typesPath/params-types";
import { submitWorkOrder } from "api/common-api";
import { RESOURCE_TYPE_LIST, LEVEL_MAP } from "constants/common";
import { staffRuleProps } from "constants/table";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ApplyClusterModal = (props: {
  dispatch: any;
  cb: Function;
  app: AppState;
  user: UserState;
  params: any;
}) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "project",
        label: "所属项目",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.app.appInfo()?.name} />,
      },
      {
        key: "name",
        label: "集群名称",
        attrs: {
          placeholder: "请填写集群名称",
          style: { width: "60%" },
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (!value || !new RegExp(regClusterName).test(value)) {
                return Promise.reject('请填写集群名称，支持大、小写字母、数字、-、_');
              }
              if (value && value.length > 128) {
                return Promise.reject('最大限制128字符');
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "type",
        label: "集群类型",
        type: FormItemType.select,
        options: RESOURCE_TYPE_LIST,
        rules: [
          {
            required: true,
            message: "请选择",
          },
        ],
        attrs: {
          style: { width: "60%" },
        },
      },
      {
        key: "level",
        label: "业务等级",
        type: FormItemType.select,
        options: LEVEL_MAP,
        rules: [
          {
            required: true,
            message: "请选择",
          },
        ],
        attrs: {
          style: { width: "60%" },
        },
      },
      {
        key: "responsible",
        label: "责任人",
        rules: [
          {
            required: true,
            ...staffRuleProps,
          },
        ],
        isCustomStyle: true,
        CustomStyle: { marginBottom: 0 },
        type: FormItemType.custom,
        customFormItem: <StaffSelect style={{ width: "60%" }} />,
      },
      {
        key: "datanode",
        label: "Datanode",
        type: FormItemType.custom,
        customFormItem: <DataNode />,
        rules: [
          {
            required: true,
            whitespace: true,
            validator: async (
              rule: any,
              value: { dataNodeNu: number; dataNodeSpec: string }
            ) => {
              if (!value) {
                return Promise.reject('请输入节点规格');
              }
              if (value?.dataNodeSpec.length == 0 || value?.dataNodeNu == 0) {
                return Promise.reject("请输入节点个数");
              } else {
                return Promise.resolve();
              }
            },
          },
        ],
      },
      {
        key: "memo",
        type: FormItemType.textArea,
        label: "集群描述",
        attrs: {
          placeholder: "请填写集群描述",
        },
        rules: [
          {
            required: false,
            validator: async (rule: any, value: string) => {
              if (value && value.length > 100) {
                return Promise.reject('请输入0-100字符');
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "description",
        type: FormItemType.textArea,
        label: "申请原因",
        attrs: {
          placeholder: "请填写申请原因",
        },
        rules: [
          {
            required: true,
            whitespace: true,
            validator: async (rule: any, value: string) => {
              if (value?.trim().length > 0 && value?.trim().length < 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入1-100字符");
              }
            },
          },
        ],
      },
    ] as IFormItem[],
    visible: true,
    title: "申请集群",
    formData: props.params || {},
    isWaitting: true,
    width: 800,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      result.responsible = Array.isArray(result.responsible)
        ? result.responsible.join(",")
        : result.responsible;
      const params: IWorkOrder = {
        contentObj: {
          name: result.name,
          dataNodeSpec: result.datanode.dataNodeSpec,
          dataNodeNu: result.datanode.dataNodeNu,
          responsible: result.responsible,
          memo: result.memo,
          type: result.type,
          level: result.level,
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description || "",
        type: "logicClusterCreate",
      };
      submitWorkOrder(params, () => {
        props.dispatch(actions.setModalId(""));
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

export default connect(mapStateToProps)(ApplyClusterModal);
