import React, { useEffect, useState } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { StaffSelect } from "container/staff-select";
import { getPackageList } from "api/cluster-api";
import { DataNode, RenderText } from "container/custom-form";
import { AppState, UserState } from "store/type";
import { IWorkOrder } from "typesPath/params-types";
import { submitWorkOrder } from "api/common-api";
import { RESOURCE_TYPE_LIST, LEVEL_MAP } from "constants/common";
import { staffRuleProps } from "constants/table";
import { Tooltip } from "antd";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ApplyClusterModal = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const [versionList, setVersionList] = useState([]);

  useEffect(() => {
    getVersionList();
  }, []);

  const getVersionList = async () => {
    let ret = await getPackageList();
    let list = ret.map((item) => {
      return {
        ...item,
        value: item.esVersion,
      };
    });
    setVersionList(list);
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "name",
        label: "集群名称",
        attrs: {
          placeholder: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
        },
        rules: [
          {
            required: true,
            message: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
            validator: async (rule: any, value: string) => {
              const reg = /^[a-zA-Z0-9_-]{1,}$/g;
              if (!reg.test(value) || value?.length > 32 || !value) {
                return Promise.reject("请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "esVersion",
        label: "集群版本",
        type: FormItemType.select,
        options: versionList || [],
        rules: [
          {
            required: true,
            message: "请选择",
          },
        ],
        attrs: {
          placeholder: "请选择版本",
        },
      },
      {
        key: "type",
        label: (
          <div className="cluster-label">
            集群类型
            <Tooltip
              title={
                <>
                  <div>独立集群：支持集群层面的数据隔离</div>
                  <div>独享集群：支持数据节点层面的隔离</div>
                  <div>共享集群：数据共享</div>
                </>
              }
            >
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.select,
        options: RESOURCE_TYPE_LIST,
        rules: [
          {
            required: true,
            message: "请选择",
          },
        ],
      },
      {
        key: "level",
        label: (
          <div className="cluster-label">
            业务等级
            <Tooltip title="请根据集群实际业务等级进行选择，这里按照业务等级高低细分为核心、重要、一般">
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.select,
        options: LEVEL_MAP,
        rules: [
          {
            required: true,
            message: "请选择",
          },
        ],
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
            validator: async (rule: any, value: { dataNodeNu: number; dataNodeSpec: string }) => {
              if (!value) {
                return Promise.reject("请输入节点规格");
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
          placeholder: "请输入0-100字集群描述",
        },
        rules: [
          {
            required: false,
            validator: async (rule: any, value: string) => {
              if (value && value.length > 100) {
                return Promise.reject("请输入0-100字集群描述");
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
          placeholder: "请输入1-100字申请原因",
        },
        rules: [
          {
            required: true,
            whitespace: true,
            validator: async (rule: any, value: string) => {
              if (value?.trim().length > 0 && value?.trim().length < 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入1-100字申请原因");
              }
            },
          },
        ],
      },
    ] as IFormItem[],
    visible: true,
    title: "申请集群",
    formData: { type: RESOURCE_TYPE_LIST[1].value, ...props.params },
    isWaitting: true,
    width: 480,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const params: IWorkOrder = {
        contentObj: {
          name: result.name,
          dataNodeSpec: result.datanode.dataNodeSpec,
          dataNodeNu: result.datanode.dataNodeNu,
          memo: result.memo,
          type: result.type,
          level: result.level,
        },
        submitorProjectId: props.app.appInfo()?.id,
        submitor: props.user.getName("userName"),
        description: result.description || "",
        type: "logicClusterCreate",
      };
      return submitWorkOrder(params, props.params?.history, () => {
        props.dispatch(actions.setModalId(""));
      });
    },
    type: "drawer",
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(ApplyClusterModal);
