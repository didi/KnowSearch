import React from "react";
import { Modal, Form, Input, message } from "antd";
import { connect } from "react-redux";
import * as actions from "actions";
import { InfoCircleOutlined } from "@ant-design/icons";
import { submitWorkOrder } from "api/common-api";
import store from "store";
import "./deleteStyle.less";

const appInfo = {
  app: store.getState().app,
  user: store.getState().user,
};

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const InstallPlugin = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;
  const [form] = Form.useForm();
  return (
    <>
      <Modal
        visible={true}
        title={"安装插件"}
        width={480}
        onCancel={() => {
          dispatch(actions.setModalId(""));
        }}
        onOk={async () => {
          const values = await form.validateFields();
          if (values && values?.desc) {
            const contentObj = {
              operationType: 3,
              logicClusterId: params.id,
              logicClusterName: params.name,
              plugIds: params.id,
              plugName: params.name,
              pluginId: params.id,
              pluginFileName: params.name,
              url: params.url,
              plugDesc: params.plugDesc,
              type: "6",
            };
            const param = {
              contentObj,
              submitorProjectId: appInfo.app.appInfo()?.id,
              submitor: appInfo.user.getName("userName"),
              description: values?.desc,
              type: "clusterOpPluginRestart",
            };
            return submitWorkOrder(param, () => {
              dispatch(actions.setModalId(""));
              cb();
            });
          }
        }}
      >
        <div>
          <div className="delete-modal-content">
            <div className="delete-modal-content-left">
              <InfoCircleOutlined className="delete-modal-content-left-icon" />
            </div>
            <div className="delete-modal-content-right">
              <p className="delete-modal-content-right-p1">是否确定安装该{params.name}插件？</p>
              <p className="delete-modal-content-right-p2">插件卸载、安装需要重启集群，点击确认后，将自动提交工单。</p>
            </div>
          </div>

          <div style={{ marginTop: 10 }}>
            <Form form={form} layout="vertical">
              <Form.Item
                label="申请理由"
                style={{ marginBottom: 0 }}
                rules={[
                  {
                    required: true,
                    validator: (rule: any, value: string) => {
                      if (!value || value?.trim().length > 100) {
                        return Promise.reject("请输入1-100字申请原因");
                      } else {
                        return Promise.resolve();
                      }
                    },
                  },
                ]}
                name={"desc"}
              >
                <Input.TextArea allowClear rows={4} placeholder="请输入申请原因1-100个字符" />
              </Form.Item>
            </Form>
          </div>
        </div>
      </Modal>
    </>
  );
});
