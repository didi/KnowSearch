import React, { useState, useEffect } from "react";
import { Table, Button, Dropdown, Form, Input } from "antd";
import { cloneDeep } from "lodash";
import { regNonnegativeInteger } from "constants/reg";
import "./index.less";

export const InstallPluginTable = (props: any) => {
  const { data, index, configList, setConfigList, plugType } = props;
  const [moveData, setMoveData] = useState([]);
  const [dataSource, setDataSource] = useState(data);

  useEffect(() => {
    getMoveData();
  }, [configList]);

  const getMoveData = () => {
    let list = cloneDeep(configList);
    list.splice(index, 1);
    setMoveData(list);
  };

  const moveNode = (target, record) => {
    let list = configList.map((item, itemIndex) => {
      if (item.groupName === target.groupName) {
        item.hosts.push(record);
      }
      if (itemIndex === index) {
        item.hosts = item.hosts.filter((element) => element.id !== record.id);
      }
      return item;
    });
    setConfigList(cloneDeep(list));
  };

  useEffect(() => {
    onChange();
  }, [dataSource]);

  const onChange = () => {
    const newConfigList = [...configList];
    newConfigList[index].hosts = dataSource;
    setConfigList(newConfigList);
  };

  const columns = [
    {
      title: "IP:端口号",
      dataIndex: "ip",
      key: "ip",
      width: 150,
    },
    {
      title: "角色",
      dataIndex: "node",
      key: "node",
      width: 150,
    },
    {
      title: "安装目录",
      dataIndex: "install",
      key: "install",
      width: 180,
      render: (val, record) => {
        if (plugType === "engine") {
          return val;
        }
        return (
          <Form.Item
            className="install"
            name={`install-${record.id}`}
            key={`install-${record.id}`}
            initialValue={val || undefined}
            rules={[
              {
                validator: (rule: any, value: string) => {
                  if (!value) return Promise.reject("请输入安装目录");
                  if (value?.length > 128) {
                    return Promise.reject("最大支持128个字符");
                  }
                  let list = dataSource.map((item) => {
                    if (item.id === record?.id) {
                      item.install = value;
                    }
                    return item;
                  });
                  setDataSource(list);
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Input placeholder="请输入"></Input>
          </Form.Item>
        );
      },
    },
    {
      title: "操作",
      key: "action",
      width: 80,
      render: (_, record) => {
        // 引擎插件或配置组只有一个时，无移动操作
        if (plugType === "engine" || configList?.length === 1) return "-";
        return (
          <Dropdown
            overlayClassName="move-dropdown"
            placement="bottomRight"
            overlay={
              <div className="move-content">
                {(moveData || []).map((item) => {
                  return (
                    <div
                      key={item.key}
                      className="move-item"
                      onClick={() => {
                        moveNode(item, record);
                      }}
                    >
                      {item?.groupName}
                    </div>
                  );
                })}
              </div>
            }
            getPopupContainer={(node) => node.parentElement}
            trigger={["click"]}
          >
            <Button type="link">移动</Button>
          </Dropdown>
        );
      },
    },
  ];
  return <Table className="install-plugin-table" columns={columns} dataSource={data} rowKey="id" pagination={false}></Table>;
};
