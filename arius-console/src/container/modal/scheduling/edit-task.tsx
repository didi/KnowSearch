import React, { useEffect, useState } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { RenderText } from "container/custom-form";
import { message } from "antd";
import { getWorkerList, updateTask } from "api/Scheduling";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const EditTask = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const { params } = props;
  const [workList, setWorkList] = useState([]);

  useEffect(() => {
    _getWorkerList();
  }, []);

  const _getWorkerList = async () => {
    let list = await getWorkerList();
    let workList = (list || []).map((item) => ({ value: item, label: item }));
    setWorkList(workList);
  };

  const getFormData = () => {
    let cluster = params?.params ? JSON.parse(params?.params)?.esClusterNames : undefined;
    let formData = {
      cluster,
      workerIps: params?.workerIps || undefined,
    };
    return formData;
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "taskDesc",
        label: "任务名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={params.taskDesc} />,
      },
      {
        key: "workerIps",
        label: "执行器",
        type: FormItemType.select,
        options: workList,
        attrs: { mode: "multiple", placeholder: "请选择执行器", maxTagCount: "responsive" },
        rules: [{ required: true, message: "请选择执行器" }],
      },
      {
        key: "cluster",
        label: "目标集群",
        type: FormItemType.select,
        options: (params?.clusterList || []).map((item) => ({ value: item, label: item })),
        attrs: { mode: "multiple", placeholder: "请选择目标集群", maxTagCount: "responsive" },
        rules: [{ required: true, message: "请选择目标集群" }],
      },
    ] as IFormItem[],
    formData: getFormData(),
    className: "edit-task",
    visible: true,
    title: "编辑",
    needBtnLoading: true,
    needSuccessMessage: false,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: any) => {
      let params = {
        workerIps: result.workerIps,
        param: JSON.stringify({ esClusterNames: result.cluster }),
      };
      await updateTask(props.params.taskCode, params);
      message.success("编辑成功");
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
