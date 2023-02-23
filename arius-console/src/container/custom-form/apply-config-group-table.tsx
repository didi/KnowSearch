import React, { useState, useEffect } from "react";
import { Form, Select, Table, Input, Button, Modal } from "antd";
import { regIp, regNonnegativeInteger } from "constants/reg";
import { uuid } from "lib/utils";
import { XModal } from "component/x-modal";
import "./index.less";

export const ApplyConfigGroupList = (props: any) => {
  const { data, machineList, index, configList, setConfigList } = props;
  const [dataSource, setDataSource] = useState([]);
  const [visible, setVisible] = useState(false);
  const [importFirst, setImportFirst] = useState({ machineSpec: false });

  const [form] = Form.useForm();

  useEffect(() => {
    getDataSource();
  }, [data]);

  useEffect(() => {
    onChange();
  }, [dataSource]);

  const getDataSource = () => {
    let install = "";
    try {
      install = JSON.parse(data?.systemConfig[0]?.value)?.installDirector;
    } catch {}
    let id = uuid();
    let empty = [{ id, ip: "", machineSpec: "", install }];
    setDataSource(data?.length ? data : empty);
  };
  const addData = () => {
    let id = uuid();
    let length = dataSource.length;
    let install = "";
    try {
      install = JSON.parse(data?.systemConfig[0]?.value)?.installDirector;
    } catch {
      install = dataSource[length - 1]?.install || "";
    }
    let node = {
      id,
      ip: "",
      machineSpec: dataSource[length - 1]?.machineSpec || "",
      install,
    };
    setDataSource([...dataSource, node]);
  };

  const onChange = () => {
    const newConfigList = [...configList];
    newConfigList[index].ipList = dataSource;
    setConfigList(newConfigList);
  };

  const getColumns = () => {
    let columns = [
      {
        title: "主机名/IP",
        dataIndex: "ip",
        key: "ip",
        width: 180,
        render: (val, record) => {
          return (
            <Form.Item
              className="ip"
              name={`ip-${record.id}`}
              key={`ip-${record.id}`}
              initialValue={val || undefined}
              rules={[
                {
                  validator: (rule: any, value: string) => {
                    if (!value) return Promise.reject("请输入主机名/IP");
                    if (!new RegExp(regIp).test(value)) {
                      return Promise.reject("请输入正确格式");
                    }
                    for (let i = 0; i < dataSource?.length; i++) {
                      if (dataSource[i]?.ip === value && dataSource[i]?.id !== record?.id) {
                        return Promise.reject("ip重复，请更改");
                      }
                    }
                    let list = dataSource.map((item) => {
                      if (item.id === record?.id) {
                        item.ip = value;
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
        title: "机型",
        dataIndex: "machineSpec",
        key: "machineSpec",
        width: 180,
        render: (val, record) => {
          return (
            <Form.Item
              className="machine"
              name={`machineSpec-${record.id}`}
              key={`machineSpec-${record.id}`}
              initialValue={val || undefined}
              rules={[
                {
                  validator: (rule: any, value: string) => {
                    if (!value) return Promise.reject("请选择");
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
              <Select
                placeholder="请选择"
                options={machineList}
                onChange={(val) => {
                  if (importFirst.machineSpec) {
                    XModal({
                      type: "info",
                      title: `是否全部应用？`,
                      onOk: () => {
                        let newDataSource = dataSource.map((item) => ({ ...item, machineSpec: val, id: uuid() }));
                        setDataSource(JSON.parse(JSON.stringify(newDataSource)));
                      },
                    });
                  }
                  let first = { ...importFirst, machineSpec: false };
                  setImportFirst(first);
                }}
              ></Select>
            </Form.Item>
          );
        },
      },
      {
        title: "安装目录",
        dataIndex: "install",
        key: "install",
        width: 180,
        render: (val, record) => {
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
                        item.install = value.trim();
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
    return columns;
  };

  return (
    <>
      <Table className="apply-config-group" columns={getColumns()} dataSource={dataSource} rowKey="id" pagination={false}></Table>
      <Button className="import-config-group" type="link" onClick={() => setVisible(true)}>
        <span className="add">+ </span>批量导入
      </Button>
      {visible && (
        <Modal
          className="import-ip-modal"
          visible={visible}
          title={"批量导入"}
          onCancel={() => setVisible(false)}
          onOk={() => {
            let list = form.getFieldValue("ipList")?.split("\n");
            let ipList = list.filter((item) => item !== "");
            let install = "";
            try {
              install = JSON.parse(data?.systemConfig[0]?.value)?.installDirector;
            } catch {}
            let importList = ipList.map((item) => ({ id: uuid(), ip: item, machineSpec: "", install }));
            let newDataSource = dataSource.length === 1 && !dataSource[0]?.ip ? importList : [...dataSource, ...importList];
            setDataSource(newDataSource);
            setVisible(false);
            setImportFirst({ machineSpec: true });
          }}
        >
          <Form form={form}>
            <Form.Item
              name="ipList"
              rules={[
                {
                  validator: async (_, value) => {
                    let ipList = value.split("\n");
                    for (let i = 0; i < ipList.length; i++) {
                      if (ipList[i] && !new RegExp(regIp).test(ipList[i])) {
                        return Promise.reject("请输入正确格式");
                      }
                    }
                    return Promise.resolve("");
                  },
                },
              ]}
            >
              <Input.TextArea placeholder="请输入节点ip，不同ip换行分隔"></Input.TextArea>
            </Form.Item>
          </Form>
        </Modal>
      )}
    </>
  );
};
