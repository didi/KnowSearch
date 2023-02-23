import React, { useState, useEffect } from "react";
import { Form, Table, Input } from "antd";
import { uuid } from "lib/utils";
import "./index.less";

export const SystemRunningTable = (props: any) => {
  const { data, index, configList, setConfigList, activeRadioKey } = props;
  const [dataSource, setDataSource] = useState([]);

  useEffect(() => {
    getDataSource();
  }, [activeRadioKey]);

  useEffect(() => {
    onChange();
  }, [dataSource]);

  const getDataSource = () => {
    let id = uuid();
    let empty = [{ id, key: "", value: "" }];
    let config = {};
    try {
      config = activeRadioKey === 2 ? JSON.parse(data?.systemConfig[0]?.value) : JSON.parse(data?.runningConfig[0]?.value);
    } catch {}
    let keys = Object.keys(config);
    let list = keys.map((item) => ({ id: uuid(), key: item, value: config[item] }));
    setDataSource(keys?.length ? list : empty);
  };

  const addData = () => {
    let id = uuid();
    let node = {
      id,
      key: "",
      value: "",
    };
    setDataSource([...dataSource, node]);
  };

  const onChange = () => {
    const newConfigList = [...configList];
    let obj = {};
    dataSource.forEach((item) => {
      obj[item?.key] = item?.value;
    });
    activeRadioKey === 2
      ? (newConfigList[index].systemConfig[0].value = JSON.stringify(obj))
      : (newConfigList[index].runningConfig[0].value = JSON.stringify(obj));
    setConfigList(newConfigList);
  };

  const columns = [
    {
      title: "Key",
      dataIndex: "key",
      key: "key",
      width: 150,
      render: (val, record) => {
        return (
          <Form.Item
            className="key"
            name={`key-${record.id}`}
            key={`key-${record.id}`}
            initialValue={val || undefined}
            rules={[
              {
                validator: (rule: any, value: string) => {
                  let list = dataSource.map((item) => {
                    if (item.id === record?.id) {
                      item.key = value;
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
      title: "Value",
      dataIndex: "value",
      key: "value",
      width: 180,
      render: (val, record) => {
        return (
          <Form.Item
            className="value"
            name={`value-${record.id}`}
            key={`value-${record.id}`}
            initialValue={val || undefined}
            rules={[
              {
                validator: (rule: any, value: string) => {
                  let list = dataSource.map((item) => {
                    if (item.id === record?.id) {
                      item.value = value;
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
      width: 100,
      render: (_, record) => {
        return (
          <div className="action">
            {dataSource.length <= 1 ? null : (
              <svg
                onClick={() => {
                  let data = dataSource.filter((item) => item?.id !== record?.id);
                  setDataSource(data);
                }}
                className="icon svg-icon delete-row"
                aria-hidden="true"
              >
                <use xlinkHref="#iconjianshao"></use>
              </svg>
            )}
            <svg onClick={addData} className="icon svg-icon add-row" aria-hidden="true">
              <use xlinkHref="#iconzengjia"></use>
            </svg>
          </div>
        );
      },
    },
  ];
  return <Table className="apply-config-group" columns={columns} dataSource={dataSource} rowKey="id" pagination={false}></Table>;
};
