import React from "react";
//import { Modal, Form, Input, message } from 'antd';
import { Modal, message } from "knowdesign";
const { confirm } = Modal;
import { connect } from "react-redux";
import * as actions from "actions";
import { IconFont } from "@knowdesign/icons";
//import { submitWorkOrder } from "api/common-api";
import { clusterDelete } from "api/cluster-api";
import store from "store";
import "./deleteStyle.less";

const loginInfo = {
  userName: store.getState().user?.getName,
  app: store.getState().app,
};

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const DeleteCluster = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;
  confirm({
    title: `确定要删除物理集群${params.cluster}？`,
    icon: <IconFont type="icon-shibai" />,
    content: `集群删除后，集群所有相关数据也将被删除，请谨慎操作！`,
    okText: "确定",
    cancelText: "取消",
    onOk: async () => {
      const param: any = {
        regionId: params.id,
      };
      clusterDelete(param).then(() => {
        message.success("下线成功");
        cb();
        dispatch(actions.setModalId(""));
      });
    },
    onCancel: () => {
      dispatch(actions.setModalId(""));
    },
  });
  return (
    <>
      {/* <Modal
        visible={true}
        title={"集群下线"}
        centered
        maskClosable={false}
        width={480}
        onCancel={() => {
          dispatch(actions.setModalId(""));
        }}
        onOk={async () => {
          const param: any = {
            regionId: params.id,
          };
          clusterDelete(param).then(() => {
            message.success("下线成功");
            cb();
            dispatch(actions.setModalId(""));
          });
        }}
      >
        <div>
          <div className="delete-modal-content">
            <div className="delete-modal-content-left">
              <InfoCircleOutlined className="delete-modal-content-left-icon" />
            </div>
            <div className="delete-modal-content-right">
              <p className="delete-modal-content-right-p1">是否确定删除物理集群{params.cluster}？</p>
              <p className="delete-modal-content-right-p2">集群删除后，集群所有相关数据也将被删除，请谨慎操作！</p>
            </div>
          </div>
        </div>
      </Modal> */}
    </>
  );
});
