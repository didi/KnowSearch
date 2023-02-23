import React, { createRef } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { Select, Tooltip, message } from "antd";
import { updateAllocation } from "api/cluster-api";
import { filterOption } from "lib/utils";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const BatchAllocation = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const $formRef: any = createRef();
  const allocationList = [
    { value: "cluster.routing.allocation.node_concurrent_incoming_recoveries", default: "2", tips: "节点shard并发恢复数限制" },
    { value: "indices.recovery.max_bytes_per_sec", default: "40mb", tips: "集群节点shard分配的磁盘高水位限制" },
    { value: "cluster.routing.allocation.disk.watermark.high", default: "90%", tips: "节点索引恢复的流量限制" },
  ];

  const onSelectChange = (value: any) => {
    const defaultValue = allocationList.find((item) => item.value === value)?.default;
    $formRef?.current?.form.setFieldsValue({ value: defaultValue });
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "key",
        label: "动态配置",
        type: FormItemType.custom,
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
        defaultValue: allocationList[0].value,
        customFormItem: (
          <Select
            showSearch
            filterOption={(input, option: any) => option.key.toLowerCase().indexOf(input.toLowerCase()) >= 0}
            placeholder="请选择"
            onChange={onSelectChange}
          >
            {allocationList.map((item, index) => (
              <Select.Option key={item.value || index} value={item.value}>
                <Tooltip placement="bottomLeft" title={item.tips}>
                  <div style={{ width: "100%" }}>{item.value}</div>
                </Tooltip>
              </Select.Option>
            ))}
          </Select>
        ),
        rules: [{ required: true, message: "请选择动态配置" }],
      },
      {
        key: "value",
        label: "值",
        type: FormItemType.input,
        defaultValue: allocationList[0].default,
        rules: [{ required: true, message: "请输入值" }],
      },
      {
        key: "ids",
        label: "操作对象",
        type: FormItemType.custom,
        customFormItem: (
          <div className="btn-labels-box">
            {props.params?.length
              ? props.params.map((item, index) => (
                  <div key={index} className="btn-labels">
                    {item.cluster}
                  </div>
                ))
              : "-"}
          </div>
        ),
      },
    ] as IFormItem[],
    formData: {
      status: true,
    },
    visible: true,
    title: "动态配置批量变更",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      if (!props.params?.length) {
        props.dispatch(actions.setModalId(""));
        return;
      }
      const clusters = props.params?.map((item) => item.cluster);

      return updateAllocation({
        clusterNameList: clusters,
        key: result.key,
        value: result.value,
      }).then(() => {
        message.success(`操作成功`);
        props.dispatch(actions.setModalId(""));
        props.cb && props.cb();
      });
    },
  };

  return (
    <>
      <XFormWrapper ref={$formRef} {...xFormModalConfig} />
    </>
  );
});
