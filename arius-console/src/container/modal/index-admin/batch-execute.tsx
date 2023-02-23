import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { openOrCloseReadOrWrite, indicesOpen, indicesClose } from "api/index-admin";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const BatchExecute = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const { type, datas } = props.params;
  let label = "";

  switch (type) {
    case "read":
      label = `索引读`;
      break;
    case "write":
      label = `索引写`;
      break;
    case "status":
      label = `索引状态`;
      break;
    default:
      break;
  }

  const xFormModalConfig = {
    formMap: [
      {
        key: "status",
        label: label,
        type: FormItemType._switch,
        formAttrs: {
          style: {
            marginTop: 0,
          },
        },
      },
      {
        key: "ids",
        label: "操作对象",
        type: FormItemType.custom,
        customFormItem: (
          <div className="btn-labels-box">
            {datas.length
              ? datas.map((item, index) => (
                  <div key={index} className="btn-labels">
                    {item.index}
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
    title: "批量操作",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      if (!datas.length) {
        props.dispatch(actions.setModalId(""));
        return;
      }
      const thenFn = () => {
        message.success(`操作成功`);
        props.dispatch(actions.setModalId(""));
        props.cb && props.cb(); // 重新获取数据列表
      };
      if (type === "read" || type === "write") {
        const params = datas?.map((item) => ({
          cluster: item.cluster,
          index: item.index,
          type: type,
          value: !result.status,
        }));
        return openOrCloseReadOrWrite(params).then(() => {
          thenFn();
        });
      } else if (type === "status") {
        const params = datas?.map((item) => ({
          cluster: item.cluster,
          index: item.index,
        }));
        const submitFn = result.status ? indicesOpen : indicesClose;
        return submitFn(params).then(() => {
          thenFn();
        });
      }
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
