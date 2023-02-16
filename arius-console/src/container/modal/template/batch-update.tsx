import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message, Tooltip } from "antd";
import { regNonnegativeInteger } from "constants/reg";
import { updateTemplateSrv } from "api/cluster-index-api";
import { CodeType } from "constants/common";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const BatchUpdate = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const { code, datas } = props.params;
  const [status, setStatus] = React.useState(true);
  const ids = [];
  let label: string | React.ReactNode;

  datas.forEach((item) => {
    const unavailableSrv = item.unavailableSrv?.find((srv) => srv.srvCode === code);
    if (!unavailableSrv) {
      ids.push({
        name: item.name,
        id: item.id,
        expireTime: item.expireTime,
      });
    }
  });
  const expireTimes = ids.map((item) => (item.expireTime !== -1 ? item.expireTime : null)).sort();

  switch (code) {
    case CodeType.PreCreate:
      label = `预创建`;
      break;
    case CodeType.Delete:
      label = `过期删除`;
      break;
    case CodeType.Separate:
      label = `冷热分离`;
      break;
    case CodeType.Pipeline:
      label = `pipeline`;
      break;
    case CodeType.Rollover:
      label = (
        <div className="rollover-title">
          <span className="title">Rollover</span>
          <Tooltip title={"开启后会影响索引Update和Delete能力以及指定id写入、更新、删除"}>
            <span className="icon iconfont iconinfo"></span>
          </Tooltip>
        </div>
      );
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
        attrs: {
          onChange: (value) => {
            setStatus(value);
          },
        },
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
            {ids.length
              ? ids.map((item) => (
                  <div className="btn-labels" key={item.id}>
                    {item.name}
                  </div>
                ))
              : "-"}
          </div>
        ),
      },
      {
        key: "coldSaveDays",
        label: "热节点保存天数",
        type: FormItemType.inputNumber,
        invisible: !(code === CodeType.Separate && status),
        attrs: {
          placeholder: "请输入热节点保存天数",
          style: {
            width: "100%",
          },
        },
        rules: [
          {
            required: true,
            message: `请输入热节点保存天数（${expireTimes[0] ? "1-" + expireTimes[0] : "1或以上"}的正整数）`,
            validator: (rule: any, value: any) => {
              if (expireTimes[0]) {
                if (new RegExp(regNonnegativeInteger).test(value) && value > 0 && value <= expireTimes[0]) return Promise.resolve();
              } else {
                if (new RegExp(regNonnegativeInteger).test(value) && value > 0) return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
      },
    ] as IFormItem[],
    formData: {
      status: status,
    },
    visible: true,
    title: "批量操作",
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      if (!ids.length) {
        props.dispatch(actions.setModalId(""));
        return;
      }
      const params = {
        srvCode: code,
        templateIdList: ids.map((item) => item.id),
      } as any;
      if (code === CodeType.Separate && result.status) {
        params.params = {};
        params.params.coldSaveDays = result.coldSaveDays;
      }
      return updateTemplateSrv(params, result.status ? "PUT" : "DELETE").then(() => {
        message.success(`操作成功`);
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
