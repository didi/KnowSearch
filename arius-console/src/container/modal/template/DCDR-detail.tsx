import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message, Button, Modal } from "knowdesign";
import { getDCDRDetail, deleteDCDR } from "api/cluster-index-api";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const DCDRDetail = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: number }) => {
  const [detail, setDetail] = React.useState({} as any);

  React.useEffect(() => {
    getDCDRDetail(props.params)
      .then((res = {}) => {
        setDetail(res?.data || {});
      })
      .catch((res = {}) => {
        setDetail(res?.data || {});
      });
  }, []);

  const goDeleteDCDR = () => {
    Modal.confirm({
      title: "提示",
      content: "确定删除DCDR链路吗?",
      onOk: () => {
        deleteDCDR(props.params).then(() => {
          message.success(`操作成功`);
          props.dispatch(actions.setDrawerId(""));
          props.cb && props.cb(); // 重新获取数据列表
        });
      },
    });
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "masterClusterName",
        label: "集群",
        type: FormItemType.custom,
        customFormItem: detail.masterClusterName || "-",
      },
      {
        key: "slaveClusterName",
        label: "从集群",
        type: FormItemType.custom,
        customFormItem: detail.slaveClusterName || "-",
      },
      {
        key: "templateCheckPointDiff",
        label: "主从位点差",
        type: FormItemType.custom,
        customFormItem: detail.templateCheckPointDiff || detail.templateCheckPointDiff === 0 ? detail.templateCheckPointDiff : "-",
      },
      {
        key: "deleteDCDR",
        label: "",
        type: FormItemType.custom,
        customFormItem: (
          <Button danger onClick={goDeleteDCDR}>
            删除链路
          </Button>
        ),
        formAttrs: {
          style: {
            position: "absolute",
            bottom: "-6px",
            left: "170px",
          },
        },
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: "DCDR链路",
    width: 500,
    layout: "horizontal",
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onSubmit: (result: any) => {
      props.dispatch(actions.setDrawerId(""));
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
