import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { notification } from "antd";
import { StaffSelect } from "container/staff-select";
import { RESOURCE_TYPE_LIST } from "constants/common";
import {
  getvailablePhysicsClusterListLogic,
} from "api/cluster-api";
import { RelevanceRegion, RenderText } from "container/custom-form";
import { regClusterName } from "constants/reg";
import { renderTip } from "container/tooltip";
import { AppState } from "store/type";
import { staffRuleProps } from "constants/table";
import { submitWorkOrder } from "api/common-api";
import { SelectType } from "./select-type";
import { CodeSandboxCircleFilled } from "@ant-design/icons";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const NewClusterModal = (props: {
  dispatch: any;
  cb: Function;
  params: any;
  app: AppState;
  user: any;
  type: string | number,
}) => {
  const $ref: any = React.createRef();

  const getPhyClusterList = (type: number) => {
    getvailablePhysicsClusterListLogic(type).then((res) => {
      if (res) {
        res = res.map((item) => {
          return {
            value: item,
            label: item,
          };
        });
        props.dispatch(actions.setPhyClusterList(res, type + ""));
      }
    });
  };

  const formMap = [
    {
      key: "name",
      label: "集群名称",
      attrs: {
        placeholder: "请输入集群名称",
        style: { width: "60%" },
      },
      rules: [
        {
          required: true,
          whitespace: true,
          validator: async (rule: any, value: string) => {
            if (!value || !new RegExp(regClusterName).test(value)) {
              return Promise.reject('请填写集群名称，支持大、小写字母、数字、-、_');
            }
            if (value && value.length > 128) {
              return Promise.reject('最大限制128字符');
            }
          },
        },
      ],
    },
    {
      key: "project",
      label: "所属项目",
      type: FormItemType.text,
      customFormItem: <RenderText text={props.app.appInfo()?.name} />,
    },
    {
      key: "responsible",
      label: "责任人",
      colSpan: 10,
      rules: [
        {
          required: true,
          ...staffRuleProps,
        },
      ],
      isCustomStyle: true,
      type: FormItemType.custom,
      isCustomStyle: true,
      customFormItem: (
        <StaffSelect placeholder="请选择责任人" style={{ width: "60%" }} />
      ),
    },
    {
      key: "type",
      label: "集群类型",
      // type: FormItemType.select,
      // options: RESOURCE_TYPE_LIST,
      type: FormItemType.custom,
      customFormItem: <SelectType optionList={RESOURCE_TYPE_LIST} />,
      rules: [
        {
          required: true,
          validator: (rule: any, value: number, ...args) => {
            if (!value && value !== 0) {
              return Promise.reject('请选择集群类型');
            }
            getPhyClusterList(value);
            return Promise.resolve();
          },
        },
      ],
      attrs: {
        style: { width: "60%" },
      },
    },
    {
      key: "regionObj",
      type: FormItemType.custom,
      label: (
        <span>
          关联region {renderTip("请先选择集群类型，再关联region！")}{" "}
        </span>
      ),
      customFormItem: <RelevanceRegion />,
      rules: [
        {
          required: true,
          whitespace: true,
          validator: async (rule: any, value: any) => {
            const type = $ref.current?.getFieldValue("type");
            if (!type) {
              return Promise.reject("请先选择集群类型！");
            }
            if (!value) {
              return Promise.reject("请关联region，并点击添加完成操作！");
            } else {
              if (
                type !== 1 &&
                value.length > 1
              ) {
                return Promise.reject(
                  "非共享集群只可选择一个物理集群，请删除。"
                );
              }
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      key: "memo",
      type: FormItemType.textArea,
      label: "集群描述",
      rules: [
        {
          whitespace: true,
          validator: async (rule: any, value: string) => {
            if (!value || value?.trim().length <= 100) {
              return Promise.resolve();
            } else {
              return Promise.reject("请输入0-100字描述信息");
            }
          },
        },
      ],
      attrs: {
        placeholder: "请输入0-100字集群描述",
        style: { width: "100%" },
      },
    },
    {
      key: "description",
      type: FormItemType.textArea,
      label: "申请原因",
      rules: [
        {
          required: true,
          whitespace: true,
          validator: async (rule: any, value: string) => {
            if (value?.trim().length > 0 && value?.trim().length < 100) {
              return Promise.resolve();
            } else {
              return Promise.reject("请输入1-100个字申请原因");
            }
          },
        },
      ],
      attrs: {
        placeholder: "请输入1-100字申请原因",
      },
    },
  ] as IFormItem[]

  const xFormModalConfig = {
    formMap: formMap,
    visible: true,
    title: "新建集群",
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const params = {
        contentObj: {
          responsible: Array.isArray(result.responsible)
                        ? result.responsible.join(",")
                        : result.responsible,
          clusterRegionDTOS: Array.isArray(result.regionObj) ? result.regionObj.map((item) => ({
            id: item.regionId,
            phyClusterName: item.name,
            racks: item.racks,
          })) : [],
          dataCenter : "cn",
          dataNodeNu : 0,
          dataNodeSpec : "",
          configJson : "",
          id : 0,
          libraDepartment : "",
          libraDepartmentId : "",
          quota : 0,
          name: result.name,
          type: result.type
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description || "",
        type: "logicClusterCreate",
      }

      submitWorkOrder(params).then((res) => {
        props.dispatch(actions.setModalId(""));
      })
      .finally(() => {
        props.cb && props.cb(); // 重新获取数据列表
      });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} ref={$ref} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(NewClusterModal);
