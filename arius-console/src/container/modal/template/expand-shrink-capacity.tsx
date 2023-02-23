import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { regNonnegativeInteger } from "constants/reg";
import { getTemplateIndexDetail, updateShard } from "api/cluster-index-api";
import { IOpTemplateIndexDetail } from "typesPath/index-types";
import { XNotification } from "component/x-notification";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const ExpandShrinkCapacity = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const [formData, setFormData]: any = React.useState({});

  React.useEffect(() => {
    getTemplateIndexDetail(props.params.id).then((data: IOpTemplateIndexDetail[]) => {
      setFormData({ shard: data[0]?.shard });
    });
  }, []);

  const xFormModalConfig = {
    formMap: [
      {
        key: "shard",
        label: "shard个数",
        type: FormItemType.inputNumber,
        attrs: {
          placeholder: "请输入shard个数",
          style: {
            width: "100%",
          },
        },
        rules: [
          {
            required: true,
            message: "请输入shard个数（正整数）",
            validator: (rule: any, value: any) => {
              if (new RegExp(regNonnegativeInteger).test(value) && value > 0) return Promise.resolve();
              return Promise.reject();
            },
          },
        ],
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
      },
    ] as IFormItem[],
    visible: true,
    title: "扩缩容",
    needBtnLoading: true,
    width: 400,
    formData: formData,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      return updateShard(props.params.id, result.shard).then(() => {
        XNotification({ type: "success", message: `扩缩容成功` });
        props.dispatch(actions.setModalId(""));
        props.cb && props.cb(); // 重新获取数据列表
      });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
