import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { copyTask } from "api/Scheduling";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const CopyTask = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const { params } = props;

  const xFormModalConfig = {
    formMap: [
      {
        key: "tips",
        label: "",
        type: FormItemType.custom,
        formAttrs: {
          className: "warning-container",
        },
        customFormItem: (
          <>
            <span className="icon iconfont iconbiaogejieshi"></span>
            <span>任务复制后会生成一条新任务。新任务选择的采集器和运行集群会在原有任务中去除</span>
          </>
        ),
      },
      {
        key: "taskDesc",
        label: "任务名称",
        type: FormItemType.input,
        attrs: {
          placeholder: "请输入任务名称",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (!value) return Promise.reject("请输入任务名称");
              const reg = /^[a-zA-Z0-9\u4e00-\u9fa5_-]{1,}$/g;
              if (!reg.test(value) || value?.length > 32) {
                return Promise.reject("请输入正确的任务名称，支持中英文、数字、-、_，1-32位字符");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "workerIps",
        label: "执行器",
        type: FormItemType.select,
        options: (params?.workerIps || []).map((item) => ({ value: item, label: item })),
        attrs: { mode: "multiple", placeholder: "请选择执行器", maxTagCount: "responsive" },
        rules: [
          {
            required: true,
            validator: (rule: any, value: any) => {
              if (!value) return Promise.reject("请选择执行器");
              if (value.length === params?.workerIps?.length) {
                return Promise.reject("新任务不允许与原任务采集器一致");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "cluster",
        label: "目标集群",
        type: FormItemType.select,
        attrs: { mode: "multiple", placeholder: "请选择目标集群", maxTagCount: "responsive" },
        options: (params.params ? JSON.parse(params.params)?.esClusterNames : []).map((item) => ({ value: item, label: item })),
        rules: [
          {
            required: true,
            validator: (rule: any, value: any) => {
              let clusterList = JSON.parse(params.params)?.esClusterNames;
              if (!value) return Promise.reject("请选择目标集群");
              if (value.length === clusterList?.length) {
                return Promise.reject("新任务不允许与原任务运行集群一致");
              }
              return Promise.resolve();
            },
          },
        ],
      },
    ] as IFormItem[],
    formData: { taskDesc: params.taskDesc },
    className: "copy-task",
    visible: true,
    title: "复制",
    needBtnLoading: true,
    needSuccessMessage: false,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      let params = {
        taskDesc: result.taskDesc,
        workerIps: result.workerIps,
        param: JSON.stringify({ esClusterNames: result.cluster }),
      };
      await copyTask(props.params.taskCode, params);
      message.success("复制成功");
      props.dispatch(actions.setModalId(""));
      props.cb();
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
