import React, { useState, useEffect } from "react";
import { Form, Select, Table } from "antd";
import "./index.less";

export const ExpandShrinkConfigTable = (props: any) => {
  const { data, onChange, pluginList } = props;

  const [dataSource, setDataSource] = useState([]);
  const [plugins, setPlugins] = useState([]);

  useEffect(() => {
    getDataSource();
  }, [data]);

  useEffect(() => {
    getPlugins();
  }, [pluginList]);

  useEffect(() => {
    onChange(dataSource);
  }, [dataSource]);

  const getDataSource = () => {
    let list = data.map((item) => {
      plugins.forEach((ele) => {
        item[ele.name] = ele.options[0].value;
      });
      return item;
    });
    setDataSource(list);
  };

  const getPlugins = () => {
    let plugins = (pluginList || [])?.map((item: any) => {
      let name = item?.name;
      let options = (item?.componentGroupConfigs || [])?.map((ele) => ({ label: ele?.groupName, value: ele?.groupName }));
      return { name, options };
    });
    setPlugins(plugins);
  };

  const getColumns: any = () => {
    let columns = [
      {
        title: "扩容节点",
        dataIndex: "host",
        key: "host",
        width: 100,
        render: (val, record) => {
          let groupName = record?.config?.groupName;
          return (
            <div>
              <div>{groupName}</div>
              <div>{val}</div>
            </div>
          );
        },
      },
    ];
    plugins.forEach((item, index) => {
      let plugins = {
        title: `${item.name}`,
        dataIndex: `${item.name}-${index}`,
        key: `${item.name}-${index}`,
        width: 150,
        render: (_, record) => {
          return (
            <Form.Item
              className="expand-shrink-plugins"
              name={`plugins-${item.name}`}
              initialValue={item.options[0].value}
              rules={[
                {
                  validator: (rule: any, value: string) => {
                    dataSource.forEach((node) => {
                      if (node.hosts === record?.hosts) {
                        node[item.name] = value;
                      }
                    });
                    return Promise.resolve();
                  },
                },
              ]}
            >
              <Select placeholder="请选择" options={item.options}></Select>
            </Form.Item>
          );
        },
      };
      columns.push(plugins);
    });
    return columns;
  };

  return (
    <div className="expand-shrink-table">
      <div className="node-title">请选择节点安装插件的配置组信息:</div>
      <Table columns={getColumns()} dataSource={dataSource} rowKey="id" pagination={false}></Table>
    </div>
  );
};
