import React, { useState, useEffect } from "react";
import { debounce } from "lodash";
import { Button, message, Drawer, Form, Select, Input, Row, Col, Radio } from "antd";
import { readableForm } from "./config";
import { queryAlarmSettingDetail, createOrUpdateAlarmSetting, queryUserList } from "./service";
import classNames from "classnames";
import "./index.less";

const basicClass = "group-set-tpl-form";
enum Eflag {
  detail = "详情",
  create = "新增",
  update = "编辑",
}
const { Option } = Select;
const { TextArea } = Input;
export default function Detail(props: any) {
  const { detailVisible, flag, closeDetail, submitCb, appId, id } = props;
  const initialValues = {
    name: "",
    members: [],
    comment: "",
    status: 1,
  };
  const [submitLoading, setSubmitLoading] = useState(false);
  const [form] = Form.useForm();
  const [visible, setVisible] = useState(detailVisible);
  const [userList, setUserList] = useState([]);
  const [userMap, setUserMap] = useState({});
  const [formModel, setFormModel] = useState({
    name: "",
    members: "",
    comment: "",
    status: 1,
  });
  const onSubmit = () => {
    form
      .validateFields()
      .then((values) => {
        submitForm(values);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const submitForm = (formData: any) => {
    console.log(formData, 'formData')
    const isCreate = flag === "create";
    const members = formData.members.map((item) => `${item};${userMap[item]}`).toString();
    const params = isCreate
      ? {
          ...formData,
          members,
          appId,
        }
      : {
          ...formData,
          members,
          appId,
          id,
        };
    setSubmitLoading(true);
    createOrUpdateAlarmSetting(isCreate, params)
      .then(() => {
        message.success("提交成功");
        submitCb();
      })
      .finally(() => {
        setSubmitLoading(false);
      });
  };

  const onClose = () => {
    submitCb();
  };

  const fetchDetail = async (id) => {
    const data = await queryAlarmSettingDetail(id);
    const { userList: members, ...rest } = data;
    if (flag === "detail") {
      setFormModel({
        ...rest,
        members,
      });
    } else {
      form.setFieldsValue({
        ...rest,
        members: members.map((item) => item.id),
      });
    }
  };

  const fetchUserList = async (name?: string) => {
    const data = await queryUserList(name);
    const userMapVal =
      data.length > 0 &&
      data.reduce((res, curr) => {
        res[curr.id] = curr.username;
        return res;
      }, {});
    setUserMap(userMapVal);
    setUserList(data);
  };

  const onSearchUser = (val) => {
    if (val) {
      debounce(() => {
        fetchUserList(val);
      }, 1000);
    }
  };

  const renderUserList = (list) => {
    return list.map((item) => {
      return (
        <Option key={item.id} value={item.id}>
          {item.username}
        </Option>
      );
    });
  };

  const renderReadCol = () => {
    return readableForm.map((item, i) => {
      return (
        <Col key={item.prop} span={24} className={i && `${basicClass}-readonlyText`}>
          <span className="read-lable">{item.label}：</span>
          <span className={classNames("read-content", item?.className)}>
            {item.render ? item.render(formModel[item.prop]) : formModel[item.prop]}
          </span>
        </Col>
      );
    });
  };

  const renderWriteCol = () => {
    return (
      <>
        <Col span={24}>
          <Form.Item label="告警组名称" name="name" rules={[{ required: true, message: "请输入告警组名称" }]}>
            <Input placeholder="请输入告警组名称" maxLength={100} />
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="告警组成员" name="members" rules={[{ required: true, message: "请选择告警组成员" }]}>
            <Select mode="multiple" allowClear showSearch onSearch={onSearchUser} placeholder="请选择告警组成员">
              {renderUserList(userList)}
            </Select>
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="描述" name="comment" rules={[{ required: true, message: "请输入告警组描述" }]}>
            <TextArea placeholder="请输入告警组描述" maxLength={512} />
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="状态" name="status" rules={[{ required: true, message: "请选择状态" }]}>
            <Radio.Group>
              <Radio value={1}>启用</Radio>
              <Radio value={0}>停用</Radio>
            </Radio.Group>
          </Form.Item>
        </Col>
      </>
    );
  };

  const renderFooter = () => {
    return (
      <div
        style={{
          textAlign: "left",
        }}
      >
        {flag === "detail" ? (
          <Button onClick={onClose} type="primary">
            关闭
          </Button>
        ) : (
          <Button onClick={onSubmit} loading={submitLoading} type="primary">
            保存
          </Button>
        )}
      </div>
    );
  };

  useEffect(() => {
    setVisible(detailVisible);
  }, [detailVisible]);

  useEffect(() => {
    if (id) {
      fetchDetail(id);
    }
    fetchUserList();
  }, []);

  return (
    <Drawer
      width="528"
      title={Eflag[flag] || ""}
      onClose={onClose}
      visible={visible}
      bodyStyle={{ paddingBottom: 80 }}
      footer={renderFooter()}
    >
      <div>
        <Form layout="vertical" initialValues={initialValues} form={form}>
          <Row>{flag === "detail" ? renderReadCol() : renderWriteCol()}</Row>
        </Form>
      </div>
    </Drawer>
  );
}
