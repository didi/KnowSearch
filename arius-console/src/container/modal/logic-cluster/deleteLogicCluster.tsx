import React from 'react';
import { Modal, Form, Input, message } from 'antd';
import { connect } from "react-redux";
import * as actions from 'actions';
import { InfoCircleOutlined } from "@ant-design/icons";
import { submitWorkOrder } from "api/common-api";
import store from "store";

const loginInfo = {
  userName: store.getState().user?.getName,
  app: store.getState().app,
};

const mapStateToProps = state => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const DeleteLogicCluster = connect(mapStateToProps)((props: { dispatch: any, params: any, cb: any }) => {
  const { params, dispatch, cb } = props;
  const [form] = Form.useForm();
  return (
    <>
      <Modal
        visible={true}
        title={'集群删除'}
        width={480}
        onCancel={() => {
          dispatch(actions.setModalId(''))
        }}
        onOk={async () => {
          const values = await form.validateFields();
          console.log(values)
          if (values && values?.desc) {
            const param = {
              contentObj: {
                id: params.id,
                name: params.name,
                type: params.type,
                responsible: params.responsible,
              },
              submitorAppid: loginInfo.app.appInfo()?.id,
              submitor: loginInfo.userName("domainAccount"),
              description: values.desc,
              type: "logicClusterDelete",
            };
            return submitWorkOrder(param, () => {
              message.success("提交工单成功");
              if (params.url) {
                params.url();
              } else {
                cb();
                dispatch(actions.setModalId(''))
              }
            });
          }
        }}
      >
        <div>
          <div className="delete-modal-content">
            <div className="delete-modal-content-left">
              <InfoCircleOutlined  className="delete-modal-content-left-icon"/>
            </div>
            <div className="delete-modal-content-right">
              <p className="delete-modal-content-right-p1">是否确定删除集群{params.name}？</p>
              <p className="delete-modal-content-right-p2">集群删除后，集群所有相关数据也将被删除，请谨慎操作！</p>
            </div>
          </div>
            
          <div style={{ marginTop: 10 }}>
            <Form 
              form={form}
              layout="vertical"
            >
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
                name={'desc'} >
                <Input.TextArea rows={4} placeholder="请输入申请原因1-100个字符"/>
              </Form.Item>
            </Form>
          </div>
        </div>
      </Modal>
    </>
  )
})