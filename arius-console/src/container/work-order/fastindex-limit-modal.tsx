import React from "react";
import { Button, Modal, Input, Form, message } from "antd";
import { updateRateLimit } from "api/fastindex-api";
import { regNonnegativeInteger } from "constants/reg";
import "./index.less";

export default function FastindexLimitModal(props) {
  const { visible, onClose, taskId, reloadData, indexMoveRate } = props;

  const onSubmit = async (result) => {
    let params = { taskReadRate: result.taskReadRate };
    await updateRateLimit(taskId, params);
    message.success("修改成功");
    await reloadData();
    onClose();
  };

  return (
    <Modal title={"修改限流值"} visible={visible} className="update-limit-modal" footer="" onCancel={onClose} destroyOnClose width={480}>
      <Form layout="vertical" onFinish={onSubmit}>
        <Form.Item
          name="taskReadRate"
          label="数据迁移速率"
          initialValue={indexMoveRate}
          rules={[
            {
              required: true,
              validator: (rule: any, value: string) => {
                if (!value || !new RegExp(regNonnegativeInteger).test(value)) {
                  return Promise.reject("请输入数据迁移速率");
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <Input placeholder="请输入" suffix="条/s" />
        </Form.Item>
        <div className="footer-container">
          <Form.Item>
            <Button className="update-cancel" onClick={onClose}>
              取消
            </Button>
            <Button className="update-submit" type="primary" htmlType="submit">
              确定
            </Button>
          </Form.Item>
        </div>
      </Form>
    </Modal>
  );
}
