import React, { useEffect, useState } from "react";
import { Modal } from "antd";
import { connect } from "react-redux";
import * as actions from "actions";
import { InfoCircleOutlined } from "@ant-design/icons";
import { deleteLogic } from "api/cluster-api";
import { XNotification } from "component/x-notification";
import "./deleteStyle.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const OfflineCluster = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;
  const [loading, setLoading] = useState(false);

  return (
    <>
      <Modal
        visible={true}
        title={"逻辑集群下线"}
        centered
        maskClosable={false}
        width={480}
        okButtonProps={{ loading: loading }}
        onCancel={() => {
          dispatch(actions.setModalId(""));
        }}
        onOk={async () => {
          setLoading(true);
          deleteLogic(params.id)
            .then(() => {
              XNotification({ type: "success", message: `逻辑集群${params.name}下线成功` });
              cb();
              dispatch(actions.setModalId(""));
            })
            .finally(() => {
              setLoading(false);
            });
        }}
      >
        <div>
          <div className="delete-modal-content">
            <div className="delete-modal-content-left2">
              <InfoCircleOutlined className="delete-modal-content-left-icon" />
            </div>
            <div className="delete-modal-content-right2">
              <p className="delete-modal-content-right-p1">是否确定删除逻辑集群{params.name}？</p>
            </div>
          </div>
        </div>
      </Modal>
    </>
  );
});
