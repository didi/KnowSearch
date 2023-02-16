import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { getDCDRCluster, getDCDRRegion, createDCDR } from "api/cluster-index-api";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const CreateDCDR = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const [clusterList, setClusterList] = React.useState([]);
  const [regionList, setRegionList] = React.useState([]);
  const $ref: any = React.createRef();

  React.useEffect(() => {
    getDCDRCluster(props.params).then((res) => {
      setClusterList(
        res?.map((item) => ({
          label: item,
          value: item,
        }))
      );
    });
  }, []);

  const xFormModalConfig = {
    formMap: [
      {
        key: "targetCluster",
        label: "从集群",
        type: FormItemType.select,
        attrs: {
          placeholder: "请选择",
          onChange: (value) => {
            getDCDRRegion(value).then((res) => {
              $ref?.current?.form.setFieldsValue({ regionId: undefined });
              setRegionList(
                res?.map((item) => ({
                  label: item.name,
                  value: item.id,
                }))
              );
            });
          },
        },
        rules: [
          {
            required: true,
            message: "请选择集群",
          },
        ],
        options: clusterList,
      },
      {
        key: "regionId",
        label: "Region",
        type: FormItemType.select,
        attrs: {
          placeholder: "请选择",
        },
        rules: [
          {
            required: true,
            message: "请选择Region",
          },
        ],
        options: regionList,
      },
    ] as IFormItem[],
    visible: true,
    title: "创建DCDR链路",
    needBtnLoading: true,
    width: 500,
    needSuccessMessage: false,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      const params = {
        templateId: props.params,
        ...result,
      };
      return createDCDR(params).then(() => {
        message.success(`链路创建操作提交成功，任务异步执行`);
        props.dispatch(actions.setModalId(""));
        props.cb && props.cb(); // 重新获取数据列表
      });
    },
  };

  return (
    <>
      <XFormWrapper ref={$ref} {...xFormModalConfig} />
    </>
  );
});
