import React, { useState, useEffect } from "react";
import { Form, Select, Table, Input } from "antd";
import { regIp } from "constants/reg";
import { uuid } from "lib/utils";
import "./index.less";

export const ExpandShrinkTable = (props: any) => {
  const { data, isExpand, options, index, onChange } = props;
  const [dataSource, setDataSource] = useState([]);
  const [originData, setOriginData] = useState([]);

  useEffect(() => {
    getDataSource();
  }, [isExpand]);

  useEffect(() => {
    onChange(dataSource, index);
  }, [dataSource]);

  const getDataSource = () => {
    let list = isExpand && !data.list[data.list.length - 1]?.type ? [...data.list, addData()] : data.list;
    setDataSource(list);
    setOriginData(list);
  };

  const addData = () => {
    let id = uuid();
    return { host: "", machineSpec: "", install: "", type: "expand", id };
  };

  const columns = [
    {
      title: "IP",
      dataIndex: "host",
      key: "host",
      width: 150,
      render: (val, record) => {
        if (record.type === "expand") {
          return (
            <Form.Item
              className="expand-shrink-host"
              name={`host-${record.id}`}
              key={`host-${record.id}`}
              initialValue={val || undefined}
              rules={[
                {
                  validator: (rule: any, value: string) => {
                    if (!value && !record.install && !record.machineSpec) {
                      return Promise.resolve();
                    } else if (!value) {
                      return Promise.reject("请输入IP");
                    }
                    if (!new RegExp(regIp).test(value)) {
                      return Promise.reject("请输入正确格式");
                    }
                    for (let i = 0; i < dataSource?.length; i++) {
                      if (dataSource[i]?.host === value && dataSource[i]?.id !== record?.id) {
                        return Promise.reject("同分组下不允许IP重复");
                      }
                    }
                    let list = dataSource.map((item) => {
                      if (item.id === record?.id) {
                        item.host = value;
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
        }
        return val;
      },
    },
    {
      title: "机型",
      dataIndex: "machineSpec",
      key: "machineSpec",
      width: 180,
      render: (val, record) => {
        if (record.type === "expand") {
          return (
            <Form.Item
              className="expand-shrink-machine"
              name={`machineSpec-${record.id}`}
              key={`machineSpec-${record.id}`}
              initialValue={val || undefined}
              rules={[
                {
                  validator: (rule: any, value: string) => {
                    if (!value && !record.host && !record.install) {
                      return Promise.resolve();
                    } else if (!value) {
                      return Promise.reject("请选择机型");
                    }
                    let list = dataSource.map((item) => {
                      if (item.id === record?.id) {
                        item.machineSpec = value;
                      }
                      return item;
                    });
                    setDataSource(list);
                    return Promise.resolve();
                  },
                },
              ]}
            >
              <Select placeholder="请选择" options={options}></Select>
            </Form.Item>
          );
        }
        return val || "-";
      },
    },
    {
      title: "安装目录",
      dataIndex: "install",
      key: "install",
      width: 150,
      render: (val, record) => {
        if (record.type === "expand") {
          return (
            <Form.Item
              className="install"
              name={`install-${record.id}`}
              key={`install-${record.id}`}
              initialValue={val || undefined}
              rules={[
                {
                  validator: (rule: any, value: string) => {
                    if (!value && !record.host && !record.machineSpec) {
                      return Promise.resolve();
                    } else if (!value) {
                      return Promise.reject("请输入安装目录");
                    }
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
        }
        return val;
      },
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_, record) => {
        if (!isExpand)
          return dataSource.length > 1 ? (
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
          ) : (
            "-"
          );
        return record?.type === "expand" ? (
          <div>
            <svg
              onClick={() => {
                let data = [...dataSource, addData()];
                setDataSource(data);
              }}
              className="icon svg-icon add-row"
              aria-hidden="true"
            >
              <use xlinkHref="#iconzengjia"></use>
            </svg>
            {dataSource.length === originData.length ? null : (
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
          </div>
        ) : null;
      },
    },
  ];
  return (
    <div className="expand-shrink-table">
      <div className="node-title">{data.groupName}</div>
      <Table columns={columns} dataSource={dataSource} rowKey="id" pagination={false}></Table>
    </div>
  );
};
