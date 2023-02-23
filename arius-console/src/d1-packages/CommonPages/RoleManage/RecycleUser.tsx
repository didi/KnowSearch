import { Button, Col, Form, Input, message, Modal, Row, Table } from "antd";
import { renderOperationBtns } from "container/custom-component";
import React, { useRef } from "react";
import { deleteUserByRoleId, queryAssignedUserByRole } from "./service";
import { XNotification } from "component/x-notification";

const RecylceUser = (props) => {
  const [data, setData] = React.useState([]);
  const primaryData = useRef([]);
  const [form] = Form.useForm();

  const fetchUserList = async () => {
    const res: [] = await queryAssignedUserByRole(props.roleId);
    const initVal = res.filter((item: any) => item.has);
    primaryData.current = initVal;
    setData(initVal);
  };

  React.useEffect(() => {
    fetchUserList();
    return () => {
      primaryData.current = null;
    };
  }, []);

  const searchUser = (values) => {
    if (!values?.user) {
      setData(primaryData.current);
      return;
    }
    const _data = data.filter((item) => item.name.includes(values.user));
    setData(_data);
  };

  const onReset = () => {
    form.setFieldsValue({ user: "" });
    setData(primaryData.current);
  };

  const getColumns = () => {
    return [
      {
        title: "序号",
        dataIndex: "id",
        key: "id",
        width: 250,
      },
      {
        title: "用户账号",
        dataIndex: "name",
        key: "name",
      },
      {
        title: "操作",
        dataIndex: "operation",
        filterTitle: true,
        key: "operation",
        width: 100,
        render: (text: string, record: any) => {
          const btns = [
            {
              clickFunc: () => {
                if (props.roleName === "管理员" && primaryData?.current?.length === 1) {
                  XNotification({ type: "error", message: "管理员角色用户数不能为0" });
                  return;
                }
                Modal.confirm({
                  title: `确认回收用户 ${record.name}？`,
                  content: <span style={{ color: "red" }}>请确认影响后再进行删除操作！</span>,
                  onOk: () => {
                    deleteUserByRoleId(props.roleId, record.id).then((res) => {
                      message.success("删除成功");
                      fetchUserList();
                      props.submitCb && props.submitCb(false);
                    });
                  },
                });
              },
              label: "删除",
            },
          ];
          return renderOperationBtns(btns, record);
        },
      },
    ];
  };

  return (
    <>
      <div>
        <Form form={form} onFinish={searchUser}>
          <Row>
            <Col span={10}>
              <Form.Item label="" name="user" rules={[{ required: true, message: "请输入用户账号" }]}>
                <Input allowClear placeholder={"请输入用户账号"} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item>
                <Button style={{ marginLeft: 16, marginRight: 16 }} type="primary" htmlType="submit">
                  查询
                </Button>
                <Button htmlType="button" onClick={onReset}>
                  重置
                </Button>
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </div>
      <Table rowKey="id" scroll={{ y: 250 }} columns={getColumns()} dataSource={data} pagination={false} />
    </>
  );
};

export default RecylceUser;
