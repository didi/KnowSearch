import { MinusCircleOutlined, PlusOutlined } from "@ant-design/icons";
import { Button, Form, InputNumber, Select, Space, Table, Tooltip, Input } from "antd";
import { getAccessClusterNodeSpecification, getNodeCount } from "api/cluster-api";
import React, { forwardRef, useState, useEffect } from "react";
import { INodeListObjet } from "container/modal/physics-cluster/apply-cluster";
import { regNonnegativeNumber, regOddNumber, regIp } from "constants/reg";
import { filterOption, uuid } from "lib/utils";
import "./index.less";

const { TextArea } = Input;

interface Props {
  onChange?: (result: any) => any;
  value?: any;
  style?: any;
  id?: any;
  host?: string;
  nodeList?: INodeListObjet;
  isHidden?: boolean;
}

export const Demo: React.FC<Props> = (props) => {
  const [tableData, setTableData] = React.useState([]);

  React.useEffect(() => {}, []);

  return <></>;
};

// 只渲染
export const RenderText = (props: any, ref) => {
  return <span>{props.value || props.text || "-"}</span>;
};

// Datanode 选择
export const DataNode: React.FC<Props> = forwardRef((props, ref) => {
  const [dataNode, setDataNode] = React.useState([]);
  const [dataNodeSpec, setDataNodeSpec] = React.useState("");
  const [dataNodeNu, setDataNodeNu] = React.useState(2);
  const [nodeCount, setNodeCount] = useState([]);

  useEffect(() => {
    _getNodeSpecification();
  }, []);

  const _getNodeSpecification = async () => {
    let res = await getAccessClusterNodeSpecification();
    let list = (res || []).map((item) => {
      return { value: item };
    });
    let count = await getNodeCount();
    let nodeCount = (count || []).map((item) => {
      return { value: item };
    });
    setDataNode(list);
    setNodeCount(nodeCount);
  };

  const handleChangeSpecification = (value) => {
    setDataNodeSpec(value);
    const { onChange } = props;
    onChange && onChange({ dataNodeSpec: value, dataNodeNu });
  };

  const handleChangeNuber = (value) => {
    const _value = value.length ? +value[value.length - 1] : (null as any);
    setDataNodeNu(_value);
    const { onChange } = props;
    onChange && onChange({ dataNodeSpec, dataNodeNu: _value });
  };

  return (
    <div className="datanode-select">
      <Select
        showSearch
        placeholder="请输入节点规格"
        onChange={handleChangeSpecification}
        filterOption={filterOption}
        style={{ width: 218, marginRight: 16 }}
      >
        {dataNode.map((v) => (
          <Select.Option value={v.value} key={v.value}>
            {(v.value + "")?.length > 35 ? (
              <Tooltip placement="bottomLeft" title={v.value}>
                {v.value}
              </Tooltip>
            ) : (
              v.value
            )}
          </Select.Option>
        ))}
      </Select>
      <Select
        placeholder="请输入节点个数"
        onChange={handleChangeNuber}
        mode="tags"
        value={(props.value?.dataNodeNu && props.value?.dataNodeNu + "") || []}
        maxTagCount={1}
        style={{ width: 218 }}
      >
        {nodeCount.map((v) => (
          <Select.Option value={v.value} key={v.value}>
            {(v.value + "")?.length > 35 ? (
              <Tooltip placement="bottomLeft" title={v.value}>
                {v.value}
              </Tooltip>
            ) : (
              v.value
            )}
          </Select.Option>
        ))}
      </Select>
    </div>
  );
});

// Masternode 选择
export const Masternode: React.FC<Props> = forwardRef((props, ref) => {
  const [options, setOption] = React.useState([]);
  const [type, setType] = React.useState("");
  const [form] = Form.useForm();

  React.useEffect(() => {}, []);

  const specificationsNode = [{ value: "datanode" }, { value: "clientnode" }];

  const onSubmit = (fields: any[], fn: any) => {
    form!
      .validateFields()
      .then((values) => {
        if (fields.length <= 1) {
          fn();
        }
      })
      .catch((err) => {});
  };

  const nodeNumberValidator = (rule, value) => {
    if (!new RegExp(regNonnegativeNumber).test(value) || value <= 1) {
      return Promise.reject();
    }
    return Promise.resolve();
  };

  const childNodeNumberValidator = (rule, value) => {
    if (!new RegExp(regOddNumber).test(value) || value < 1) {
      return Promise.reject();
    }
    return Promise.resolve();
  };

  const specValidator = (rule, value) => {
    const { nodeList } = props;
    if (value === "datanode") {
      setOption(nodeList.datanode);
    } else if (value === "clientnode") {
      setOption(nodeList.clientnode);
    } else {
      setOption([]);
    }
    setType(value);
    return Promise.resolve();
  };

  const onValuesChange = (changedValues, allValues) => {
    const { onChange } = props;
    onChange && onChange(allValues);
  };

  const { nodeList } = props;

  // Form不能含Form TODO:

  return (
    <>
      <Form layout="inline" form={form} name="control-hooks3" onValuesChange={onValuesChange}>
        {/* <Form.Item
          key="masterSpec"
          name="masterSpec"
          rules={[{ required: true, message: "请选择节点规格" }]}
        >
          <Select
            placeholder="请选择节点规格"
            style={{ width: 300, marginRight: 8 }}
          >
            {nodeList.masternode?.map((v) => (
              <Select.Option value={v.value} key={v.value}>
                {(v.value + "")?.length > 35 ? (
                  <Tooltip placement="bottomLeft" title={v.value}>
                    {v.value}
                  </Tooltip>
                ) : (
                  v.value
                )}
              </Select.Option>
            ))}
          </Select>
        </Form.Item> */}
        <Form.Item
          key="masterNodeNu"
          name="masterNodeNu"
          rules={[
            {
              required: true,
              message: "节点个数必须大于等于1，且必须为奇数。",
              validator: childNodeNumberValidator,
            },
          ]}
        >
          <InputNumber style={{ width: 250, marginTop: 10 }} placeholder="请输入节点个数"></InputNumber>
        </Form.Item>
        <Form.List name="nodeList">
          {(fields, { add, remove }) => (
            <>
              {fields.map((field) => (
                <Space
                  key={field.key}
                  align="baseline"
                  style={{
                    display: "flex",
                    alignItems: "center",
                    paddingTop: 10,
                  }}
                >
                  <Form.Item
                    noStyle
                    shouldUpdate={(prevValues, curValues) => prevValues.area !== curValues.area || prevValues.sights !== curValues.sights}
                  >
                    <Form.Item
                      {...field}
                      name={[field.name, "node"]}
                      fieldKey={[field.fieldKey, "node"]}
                      rules={[
                        {
                          required: true,
                          message: "请选择节点类型",
                          validator: specValidator,
                        },
                      ]}
                    >
                      <Select showSearch filterOption={filterOption} style={{ width: 138 }} placeholder="请选择节点类型">
                        {specificationsNode.map((v) => (
                          <Select.Option value={v.value} key={v.value} disabled={field.name === 1 && type === v.value}>
                            {(v.value + "")?.length > 35 ? (
                              <Tooltip placement="bottomLeft" title={v.value}>
                                {v.value}
                              </Tooltip>
                            ) : (
                              v.value
                            )}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  </Form.Item>

                  {/* <Form.Item
                    {...field}
                    name={[field.name, "spec"]}
                    fieldKey={[field.fieldKey, "spec"]}
                    rules={[{ required: true, message: "请选择节点规格" }]}
                  >
                    <Select style={{ width: 138 }} placeholder="请选择节点规格">
                      {options.map((v) => (
                        <Select.Option value={v.value} key={v.value}>
                          {(v.value + "")?.length > 35 ? (
                            <Tooltip placement="bottomLeft" title={v.value}>
                              {v.value}
                            </Tooltip>
                          ) : (
                            v.value
                          )}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item> */}

                  <Form.Item
                    {...field}
                    name={[field.name, "nodeNu"]}
                    fieldKey={[field.fieldKey, "nodeNu"]}
                    rules={[
                      {
                        required: true,
                        message: "节点个数必须大于等于1。",
                        validator: nodeNumberValidator,
                      },
                    ]}
                  >
                    <InputNumber style={{ width: 250 }} placeholder="请输入节点个数"></InputNumber>
                  </Form.Item>

                  <MinusCircleOutlined onClick={() => remove(field.name)} />
                </Space>
              ))}
              <Form.Item>
                <Button
                  style={{ width: 122, height: 30, margin: "10px 0 0 10px" }}
                  type="primary"
                  onClick={() => onSubmit(fields, add)}
                  block
                  icon={<PlusOutlined />}
                  disabled={fields.length > 1}
                >
                  添加节点类型
                </Button>
              </Form.Item>
            </>
          )}
        </Form.List>
      </Form>
    </>
  );
});

// 扩缩容节点列表
export const ExpandShrinkNodeList = (props: Props) => {
  const onChange = (e) => {
    props.onChange(e.target.value);
  };

  const hostList = props.host?.split("\n") || [];
  return (
    <>
      {hostList.map((item) => {
        return item ? (
          <>
            <span>{item}</span>
            <br />
          </>
        ) : null;
      })}
      {!props.isHidden ? <TextArea allowClear rows={4} onChange={onChange} placeholder={"请输入主机列表，多个主机换行"} /> : ""}
    </>
  );
};

export const DockerExpectDataNodeNu = (props) => {
  const [InputValue, setValue] = React.useState(0);
  const [isExpect, setIsExpect] = React.useState(true);

  React.useEffect(() => {
    setValue(props.value);
  }, []);

  const onChange = (value) => {
    setValue(value);
    setIsExpect(props.value - value <= 0);
    const { onChange } = props;
    onChange && onChange(value);
  };

  const { style } = props;

  return (
    <>
      <InputNumber style={style} value={InputValue} onChange={onChange} />
      <span style={{ paddingLeft: 10 }}>{`${isExpect ? "增加" : "减少"} ${Math.abs(props.podNumber - InputValue)}节点，${
        isExpect ? "扩容" : "缩容"
      }至 ${InputValue}`}</span>
    </>
  );
};

// 扩缩容节点列表
export const ExpandShrinkList = (props: any) => {
  const { data, type, isExpand, options, onShrink, onExpand, form } = props;
  const [dataSource, setDataSource] = useState([]);
  const [shrinkNode, setShrinkNode] = useState([]);
  const [expandNode, setExpandNode] = useState({});
  const [orginData, setOrginData] = useState([]);

  useEffect(() => {
    getDataSource();
    setShrinkNode([]);
  }, [isExpand]);

  useEffect(() => {
    shrinkNode.length && onShrink(shrinkNode);
  }, [shrinkNode]);

  useEffect(() => {
    typeof onExpand === "function" && onExpand(expandNode);
  }, [expandNode]);

  const getDataSource = () => {
    let list = (data || []).filter((item) => item?.role === type);
    let esClusterRoleHostVO = list[0]?.esClusterRoleHostVO;
    if (isExpand && type !== "masternode") {
      esClusterRoleHostVO = [...(esClusterRoleHostVO || []), addData()];
    }
    setOrginData(esClusterRoleHostVO);
    setDataSource(esClusterRoleHostVO);
  };

  const addData = () => {
    let id = uuid();
    let node = { id, role: type, hostname: "", machineSpec: "" };
    setExpandNode(node);
    return {
      ip: (
        <Form.Item
          className="expand-shrink-ip"
          name={`ip&${type}&${id}`}
          key={`ip&${type}&${id}`}
          rules={[
            {
              validator: (rule: any, value: string) => {
                if (!value) return Promise.resolve();
                if (!new RegExp(regIp).test(value)) {
                  return Promise.reject("请正确输入IP，例如：127.1.1.1");
                }
                for (let i = 0; i < dataSource?.length; i++) {
                  if (dataSource[i]?.ip === value) {
                    return Promise.reject("IP不能重复");
                  }
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <Input placeholder="请输入"></Input>
        </Form.Item>
      ),
      machineSpec: (
        <Form.Item
          className="expand-shrink-machine"
          name={`machineSpec&${type}&${id}`}
          key={`machineSpec&${type}&${id}`}
          rules={[
            {
              validator: (rule: any, value: string) => {
                return Promise.resolve();
              },
            },
          ]}
        >
          <Select placeholder="请选择" options={options}></Select>
        </Form.Item>
      ),
      type: "expand",
      id,
    };
  };

  const columns = [
    {
      title: "IP",
      dataIndex: "ip",
      key: "ip",
      width: 300,
    },
    {
      title: "机型",
      dataIndex: "machineSpec",
      key: "machineSpec",
      width: 200,
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_, record) => {
        if (type === "masternode") return;
        if (!isExpand)
          return (
            <svg
              onClick={() => {
                let data = dataSource.filter((item) => item?.id !== record?.id);
                setDataSource(data);
                setShrinkNode([...shrinkNode, record]);
              }}
              className="icon svg-icon delete-row"
              aria-hidden="true"
            >
              <use xlinkHref="#iconjianshao"></use>
            </svg>
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
            {dataSource.length === orginData.length ? null : (
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
    <>
      <Table columns={columns} dataSource={dataSource} rowKey="id" pagination={false}></Table>
    </>
  );
};
