import React, { useState, useEffect } from "react";
import { Button, message, Drawer, Form, Select, Input, Row, Col, Radio, TreeSelect } from "antd";
import { readableForm } from "./config";
import { debounce } from "lodash";
import { queryProjectDetail, createOrUpdateProject, queryDeptTreeData, queryUserList } from "./service";
import classNames from "classnames";
import "./index.less";
const basicClass = "project-tpl-form";
enum Eflag {
  detail = "详情",
  create = "新建",
  update = "编辑",
}

const { Option } = Select;
const { TextArea } = Input;
const { TreeNode } = TreeSelect;
export const ProjectDetail = (props: any) => {
  const { detailVisible, flag, closeDetail, refreshList, id } = props;
  const initialValues = {
    userIdList: [],
    deptId: undefined,
    deptList: [],
    description: "",
    running: true,
    projectName: "",
  };

  const [submitLoading, setSubmitLoading] = useState(false);
  const [form] = Form.useForm();
  const [visible, setVisible] = useState(detailVisible);
  const [deptList, setDeptList] = useState([]);
  const [userList, setUserList] = useState([]);
  const [formModel, setFormModel] = useState(initialValues);
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

  const submitForm = async (formData: any) => {
    setSubmitLoading(true);
    const isCreate = flag === "create";
    const data = isCreate ? { ...formData } : { ...formData, id };
    createOrUpdateProject(isCreate, data)
      .then(() => {
        message.success("提交成功");
        isCreate ? form.resetFields() : refreshList();
      })
      .finally(() => {
        setSubmitLoading(false);
      });
  };

  const onClose = () => {
    refreshList();
  };

  const fetchDetail = async (id) => {
    const data = await queryProjectDetail(id);
    if (flag === "detail") {
      setFormModel(data);
    } else {
      const fieldsValue = {
        ...data,
        userIdList: data.userList.map((item) => item.id),
      };
      form.setFieldsValue(fieldsValue);
    }
  };

  const fetchUserList = async (name?: string) => {
    const data = await queryUserList(name);
    setUserList(data);
  };

  const fetchDeptList = async (val?: string) => {
    const res = await queryDeptTreeData();
    setDeptList(res.childList);
  };

  const renderDeptTree = (list) => {
    return list.map((item) => {
      return (
        <TreeNode key={item.id} value={item.id} title={item.deptName}>
          {item.childList && item.childList.length > 0 && renderDeptTree(item.childList)}
        </TreeNode>
      );
    });
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
          <span className={classNames("read-content", item.className)}>
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
          <Form.Item label="项目名称" name="projectName" rules={[{ required: true, message: "请输入项目名称" }]}>
            <Input placeholder="请输入项目名称" maxLength={128} />
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="使用部门" name="deptId" rules={[{ required: true, message: "请选择使用部门" }]}>
            <TreeSelect
              showSearch
              treeNodeFilterProp="title"
              style={{ width: "100%" }}
              dropdownStyle={{ maxHeight: 400, overflow: "auto" }}
              placeholder="请选择使用部门"
              allowClear
            >
              {renderDeptTree(deptList)}
            </TreeSelect>
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="负责人" name="userIdList" rules={[{ required: true, message: "请选择负责人" }]}>
            <Select mode="multiple" allowClear showSearch onSearch={debounce(fetchUserList, 1000)} placeholder="请选择负责人">
              {renderUserList(userList)}
            </Select>
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="描述" name="description" rules={[{ required: true, message: "请输入描述" }]}>
            <TextArea placeholder="请输入描述" maxLength={512} />
          </Form.Item>
        </Col>
        <Col span={24}>
          <Form.Item label="状态" name="running" rules={[{ required: true, message: "请选择状态" }]}>
            <Radio.Group>
              <Radio value={true}>启用</Radio>
              <Radio value={false}>停用</Radio>
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
          <Button onClick={onSubmit} type="primary" loading={submitLoading}>
            {flag === "create" ? "保存并新增" : "保存"}
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
    if (flag !== "detail") {
      fetchUserList();
      fetchDeptList();
    }
  }, []);

  useEffect(() => {
    renderWriteCol();
  }, [formModel]);

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
};
