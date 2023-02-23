import { DeleteOutlined } from "@ant-design/icons";
import { Button, Checkbox, Drawer, message, Modal } from "antd";
import { use } from "echarts";
import React, { memo, useEffect, useState } from "react";
import { useSelector, useDispatch, shallowEqual } from "react-redux";
import "./index.less";
import * as actions from "../../../actions";
import { delIndexAdminData } from "api/index-admin";

export const DeleteIndex = memo((props) => {
  const dispatch = useDispatch();
  const [isDisabled, setIsDisabled] = useState(true);
  const { params, cb } = useSelector((state) => ({ params: (state as any).modal.params, cb: (state as any).modal.cb }), shallowEqual);

  const { delList, title, type = "下线" } = params;

  const [loading, setIsLoading] = useState(false);

  const del = async () => {
    setIsLoading(true);
    try {
      const res = await delIndexAdminData(delList);
      res ? message.success(`${type}成功`) : message.error(`${type}失败!`);
    } catch (error) {
      message.error(`${type}失败!`);
    } finally {
      setIsLoading(false);
      dispatch(actions.setModalId(""));
      // 刷新数据
      cb(true);
    }
  };

  return (
    <Modal
      visible={true}
      maskClosable={false}
      onOk={() => {
        setIsDisabled(true);
        dispatch(actions.setModalId(""));
      }}
      onCancel={() => {
        setIsDisabled(true);
        dispatch(actions.setModalId(""));
      }}
      className={"delete-index-container"}
      footer={[]}
    >
      <div className="ant-modal-confirm-title">
        <DeleteOutlined style={{ color: "red" }} />
        <p>{title}</p>
      </div>
      <p>
        <Checkbox
          onChange={() => {
            setIsDisabled(!isDisabled);
          }}
          style={{ margin: "0 15px" }}
        />
        <span>{`索引${type}后数据无法恢复，请确认影响后继续${type}操作`}</span>
      </p>
      <div className="delete-index-container-button">
        <Button key="cancel" onClick={() => dispatch(actions.setModalId(""))}>
          取消
        </Button>
        <Button
          type="primary"
          key="ok"
          disabled={isDisabled}
          loading={loading}
          onClick={() => {
            del();
          }}
          style={{ marginLeft: 8 }}
        >
          确认
        </Button>
      </div>
    </Modal>
  );
});
