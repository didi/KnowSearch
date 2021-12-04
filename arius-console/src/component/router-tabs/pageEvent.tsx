import React from "react";
import { Modal } from "antd";
import { systemKey } from '../../constants/menu';
import { QuestionCircleOutlined } from "@ant-design/icons";
import * as actions from 'actions';

export const cancelTip = (cb: (key?: string) => void, key: string, dispatch: any) => {
  Modal.confirm({
    title: "确定取消？",
    content: "取消后当前填写内容将失效，请谨慎操作",
    okText: "确定",
    cancelText: "取消",
    icon: <QuestionCircleOutlined className="question-icon" />,
    onOk: () => {
      if (cb) {
        dispatch(actions.setClearCreateIndex());
        cb(key);
      }
    },
  });
}
export const pageEventList = {
  [`menu.${systemKey}.index.create`]: cancelTip
}
