import { Button, Col, Form, Modal, Row, Select, Table } from "antd";
import { renderOperationBtns } from "container/custom-component";
import React from "react";
import { queryAssignedUserByRole } from "./service";

const BindUser = (props) => {
  const [list, setList] = React.useState([]);
  const [data, setData] = React.useState([]);

  const fetchUserList = async () => {
    const res: [] = await queryAssignedUserByRole(props.roleId);
    const initVal = res.filter((item: any) => item.has);
    const initList = res
      .filter((item: any) => !item.has)
      .map((item: any) => ({
        label: item.name,
        value: item.id,
      }));
    setList(initList);
    setData(initVal);
  };

  React.useEffect(() => {
    fetchUserList();
  }, []);

  const addUser = (values) => {
    const { user = {} } = values;
    const _data =
      data.findIndex((row) => row.id === user.value) < 0
        ? [
            ...data,
            {
              id: user.value,
              name: user.label,
            },
          ]
        : data;
    setData(_data);
    props.onUserChange(
      _data.length,
      _data.map((item) => item.id)
    );
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
          const btns = !record.has
            ? [
                {
                  clickFunc: () => {
                    Modal.confirm({
                      title: `确认删除用户 ${record.name}？`,
                      content: "",
                      onOk: () => {
                        const _data = data.filter((item) => item.id !== record.id);
                        setData(_data);
                        props.onUserChange(
                          _data.length,
                          _data.map((item) => item.id)
                        );
                      },
                    });
                  },
                  label: "删除",
                },
              ]
            : [];
          return renderOperationBtns(btns, record);
        },
      },
    ];
  };

  return (
    <>
      <div>
        <Form onFinish={addUser}>
          <Row>
            <Col span={10}>
              <Form.Item label="" name="user" rules={[{ required: true, message: "请选择用户" }]}>
                <Select
                  showSearch
                  labelInValue
                  optionFilterProp="children"
                  options={list}
                  placeholder={"请选择用户"}
                  filterOption={(input, option) => (option!.label as unknown as string).toLowerCase().includes(input.toLowerCase())}
                />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item>
                <Button style={{ marginLeft: 16 }} type="primary" htmlType="submit">
                  添加
                </Button>
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </div>
      <Table rowKey="id" scroll={{ y: 500 }} columns={getColumns()} dataSource={data} pagination={false} />
    </>
  );
};

export default BindUser;
