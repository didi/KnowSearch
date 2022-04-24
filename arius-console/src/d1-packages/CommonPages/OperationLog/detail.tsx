import React, { useState, useEffect } from "react";
import { Button, Drawer, Form, Input, Row } from "antd";
import { readableForm } from "./config";
import { queryOpLogDetail } from "./service";
import "./index.less";

const basicClass = "tpl-form";
interface IinitialValues {
  chargeUserIdList: [];
  deptId: number;
  description: string;
  isRunning: boolean;
  projectName: string;
}
const { TextArea } = Input;
export const ProjectDetail = (props: any) => {
  const { detailVisible, flag, closeDetail, submitCb, id } = props;
  const initialValues = {
    isRunning: true,
    createTime: 0,
    detail: "string",
    id: 0,
    operatePage: "string",
    operateType: "string",
    operatorIp: "string",
    operatorUsername: "string",
    target: "string",
    targetType: "string",
  };

  const [form] = Form.useForm();
  const [visible, setVisible] = useState(detailVisible);
  const [formModel, setFormModel] = useState(initialValues);
  const onClose = () => {
    closeDetail();
  };

  const fetchDetail = async (id) => {
    const data = await queryOpLogDetail(id);
    if (flag === "detail") {
      setFormModel(data);
    } else {
      form.setFieldsValue(data);
    }
  };

  const renderReadCol = () => {
    return readableForm.map((item, i) => {
      return (
        <div key={item.prop} className={`${basicClass}-readonlyText`}>
          <span className="read-lable">{item.label}：</span>
          <span className="read-content">{formModel[item.prop]}</span>
        </div>
      );
    });
  };

  const renderFooter = () => {
    return (
      <div
        style={{
          textAlign: "left",
        }}
      >
        <Button onClick={onClose} type="primary">
          关闭
        </Button>
      </div>
    );
  };

  useEffect(() => {
    setVisible(detailVisible);
  }, [detailVisible]);

  useEffect(() => {
    fetchDetail("11");
  }, []);

  return (
    <Drawer width="1000" title={"详细日志"} onClose={onClose} visible={visible} bodyStyle={{ paddingBottom: 80 }} footer={renderFooter()}>
      <div>
        <Form layout="vertical" initialValues={initialValues} form={form}>
          <Row>{renderReadCol()}</Row>
        </Form>
        <p style={{marginTop:"10px"}}>日志详情:</p>
        <TextArea value={formModel.detail} disabled placeholder="日志" autoSize={{ minRows: 20}} />
      </div>
    </Drawer>
  );
};
