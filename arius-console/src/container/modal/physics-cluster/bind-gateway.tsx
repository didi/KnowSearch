import React, { useState, useEffect } from "react";
import { getGatewayBriefInfo, bindGateway } from "api/cluster-api";
import { connect } from "react-redux";
import * as actions from "actions";
import { AppState, UserState } from "store/type";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { Modal } from "antd";
import { message } from "knowdesign";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const BindGateway = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const [options, setOptions] = useState(null);

  useEffect(() => {
    getGateway();
  }, []);

  const getGateway = async () => {
    let res = await getGatewayBriefInfo();
    let list = (res || []).map((item) => ({ value: item.id, label: item.clusterName }));
    setOptions(list);
  };

  const handleFormData = () => {
    if (props.params?.gatewayIds) {
      let ids = [];
      const gatewayIds = props.params?.gatewayIds.split(",");
      gatewayIds.forEach((item) => {
        let id = Number(item);
        if (id && !ids.includes(id)) {
          ids.push(id);
        }
      });
      return ids;
    }
  };

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
          options: options,
          placeholder: "请选择",
        },
        rules: [{ required: true }],
      },
    ] as IFormItem[],
    visible: true,
    title: "绑定Gateway",
    formData: { gateway: handleFormData() },
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (res) => {
      let gatewayClusterIds = (res.gateway || []).map((item) => +item);
      let params = { clusterPhyId: props.params.id, gatewayClusterIds };
      return bindGateway(params)
        .then(() => {
          message.success("绑定成功");
          // Modal.success({
          //   title: "绑定成功!",
          //   content: (
          //     <div>
          //       <span>
          //         如需变更查询模式，请前往
          //         <span
          //           className="project-manage"
          //           onClick={() => {
          //             Modal.destroyAll();
          //             props.params.history.push("/system/users");
          //           }}
          //         >
          //           应用管理
          //         </span>
          //         进行访问设置，创建不同的查询模式
          //       </span>
          //     </div>
          //   ),
          // });
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
