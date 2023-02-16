import React, { useEffect, useState } from "react";
import { Modal, message } from "antd";
import { connect } from "react-redux";
import * as actions from "actions";
import { InfoCircleOutlined } from "@ant-design/icons";
import { checkResources, deleteProject } from "api";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const DeleteProject = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;
  const [loading, setLoading] = useState(false);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    _checkResources();
  }, []);

  const _checkResources = async () => {
    try {
      await checkResources(params?.id);
      setVisible(true);
    } catch (err) {
      dispatch(actions.setModalId(""));
    }
  };

  return (
    <>
      <Modal
        visible={visible}
        title={"删除应用"}
        centered
        maskClosable={false}
        width={480}
        okButtonProps={{ loading: loading }}
        onCancel={() => {
          dispatch(actions.setModalId(""));
        }}
        onOk={async () => {
          setLoading(true);
          let { current, pageSize, total } = params?.pagination || {};
          deleteProject(params.id)
            .then(() => {
              message.success("删除成功");
              let pagination = {
                pageNo: current,
                pageSize,
              };
              if (current * pageSize - (total - 1) >= 10) {
                pagination.pageNo = current - 1;
              }
              cb(pagination);
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
              <p className="delete-modal-content-right-p1">确认删除应用{params.projectName}？</p>
              <p>删除后会同步下线掉关联的全部ES_User，请确认影响后再进行删除操作!</p>
            </div>
          </div>
        </div>
      </Modal>
    </>
  );
});
