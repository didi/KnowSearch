import React, { useState, useEffect } from "react";
import { bindGateway } from "api/cluster-api";
import { connect } from "react-redux";
import * as actions from "actions";
import { AppState, UserState } from "store/type";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { Modal, Button } from "antd";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const BindGateway = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const [options, setOptions] = useState(null);
  const handleFormData = () => {
    if (props.params?.gatewayUrl) {
      const gatewayUrl = props.params?.gatewayUrl.split(",");
      return gatewayUrl;
    }
  };
  // useEffect(() => {
  //获取getWayList
  //   getGateway().then((res)=>{
  //     const options=res.list;
  //     setOptions(options)
  //   })
  // }, [])
  //gataway还没有开发完成，option状态只是测试数据
  useEffect(() => {
    setOptions([
      {
        label: "gold",
        value: "gold",
      },
      {
        label: "lime",
        value: "lime",
      },
      {
        label: "green",
        value: "green",
      },
      {
        label: "cyan",
        value: "cyan",
      },
    ]);
  }, []);
  const xFormModalConfig = {
    formMap: [
      {
        key: "gateway",
        label: "Gateway名称",
        type: FormItemType.select,
        isCustomStyle: true,
        CustomStyle: { margin: 0 },
        attrs: {
          mode: "multiple",
          //options: options,
          placeholder: "请选择",
        },
        rules: [{ required: true }],
      },
    ] as IFormItem[],
    visible: true,
    title: "绑定Gateway",
    //formData: { 'gateway': props.params?.gateway || '' },
    formData: { gateway: handleFormData() },
    //isWaitting: true,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (res) => {
      const req = JSON.parse(JSON.stringify(res));
      const gatewayUrl = res.gateway.join(",");
      req["gatewayUrl"] = gatewayUrl;
      req["id"] = props.params.id;
      return bindGateway(req)
        .then(() => {
          Modal.warning({
            title: "提示",
            okText: "确定",
            content: (
              <>
                <div>
                  <span>绑定成功!</span>
                  <br />
                  <span>
                    如需变更查询模式，请前往
                    <a href="" onClick={() => props.params.history.push("/system/users")}>
                      应用管理
                    </a>
                    进行访问设置，创建不同的查询模式
                  </span>
                </div>
              </>
            ),
          });
        })
        .finally(() => {
          props.cb && props.cb();
          props.dispatch(actions.setModalId(""));
        });
    },
    width: 480,
    bodyStyle: { height: 300 },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(BindGateway);
