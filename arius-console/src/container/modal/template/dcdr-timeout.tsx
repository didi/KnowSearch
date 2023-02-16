import * as React from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Modal, Form, Input, Button, Tooltip } from "antd";
import { regNonnegativeInteger } from "constants/reg";
import "./index.less";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const DcdrTimeout = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const { onSubmit, type, opend } = props.params;
  const [form] = Form.useForm();

  const onModalSubmit = async (result) => {
    await onSubmit(type, opend, +result.timeout);
    props.dispatch(actions.setModalId(""));
  };

  return (
    <Modal
      visible={true}
      className="timeout-modal"
      footer=""
      onCancel={() => props.dispatch(actions.setModalId(""))}
      width={400}
      destroyOnClose
    >
      <Form labelCol={{ span: 8 }} wrapperCol={{ span: 15 }} form={form} onFinish={onModalSubmit}>
        <div className="timeout-container">
          <Form.Item
            name="timeout"
            label={
              <div className="timeout-title">
                <Tooltip title={"数据一致性校验超时时间"}>
                  <span className="icon iconfont iconinfo"></span>
                </Tooltip>
                <span className="title">切换超时时间</span>
              </div>
            }
            initialValue="100"
            rules={[
              { required: true, message: "请输入切换超时时间，限制数字类型，1-32位字符" },
              {
                validator: (rule: any, value: string) => {
                  if ((value && !new RegExp(regNonnegativeInteger).test(value)) || value?.length > 32) {
                    return Promise.reject(new Error("请输入切换超时时间，限制数字类型，1-32位字符"));
                  }
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Input placeholder="请输入切换超时时间，限制数字类型，1-32位字符" />
          </Form.Item>
          <span className="unit">s</span>
        </div>
        <div className="footer">
          <Form.Item>
            <Button className="cancel" onClick={() => props.dispatch(actions.setModalId(""))}>
              取消
            </Button>
            <Button type="primary" htmlType="submit">
              确定
            </Button>
          </Form.Item>
        </div>
      </Form>
    </Modal>
  );
});
