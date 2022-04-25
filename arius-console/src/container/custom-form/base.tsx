import { INode, IOpClusterRoles } from "typesPath/cluster/cluster-types";
import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  MinusCircleOutlined,
  PlusOutlined,
} from "@ant-design/icons";
import {
  Button,
  Form,
  InputNumber,
  Select,
  Space,
  Table,
  Tooltip,
  Input,
} from "antd";
import { getNodeList, getPhysicClusterRoles } from "api/cluster-api";
import { NODE_NUMBER_MAP } from "constants/status-map";
import React, { forwardRef } from "react";
import "./index.less";
import { INodeListObjet } from "container/modal/physics-cluster/apply-cluster";
import { regNonnegativeNumber, regOddNumber } from "constants/reg";

const { TextArea } = Input;

interface Props {
  onChange?: (result: any) => any;
  value?: any;
  style?: any;
  id?: any;
  host?: string;
  nodeList?: INodeListObjet;
  isHidden?: boolean
}

export const Demo: React.FC<Props> = (props) => {
  const [tableData, setTableData] = React.useState([]);

  React.useEffect(() => {}, []);

  return <></>;
};

// 只渲染
export const RenderText = forwardRef((props: any, ref) => {
  return <span>{props.value || props.text}</span>;
});

// Datanode 选择
export const DataNode: React.FC<Props> = forwardRef((props, ref) => {
  const [dataNode, setDataNode] = React.useState([]);
  const [dataNodeSpec, setDataNodeSpec] = React.useState("");
  const [dataNodeNu, setDataNodeNu] = React.useState(2);

  React.useEffect(() => {
    getNodeList().then((res) => {
      setDataNodeList(res);
    });
  }, []);

  const setDataNodeList = (data: INode[]) => {
    const list = data.map((item: INode, index: number) => {
      return {
        ...item,
        key: index,
        value: item.spec,
      };
    });
    const dataNode = list.filter((ele) => ele.role === "datanode");
    setDataNode(dataNode);
  };

  const handleChangeSpecification = (value) => {
    setDataNodeSpec(value);
    const { onChange } = props;
    onChange && onChange({ dataNodeSpec: value, dataNodeNu });
  };

  const handleChangeNuber = (value) => {
    const _value = value.length ? +value[value.length - 1] : null as any;
    setDataNodeNu(_value);
    const { onChange } = props;
    onChange && onChange({ dataNodeSpec, dataNodeNu: _value });
  };

  return (
    <>
      <Select
        placeholder="请输入节点规格"
        onChange={handleChangeSpecification}
        style={{ width: 250, marginRight: 22 }}
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
        value={props.value?.dataNodeNu || []}
        maxTagCount={1}
        style={{ width: 250 }}
      >
        {NODE_NUMBER_MAP.map((v) => (
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
    </>
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
      <Form
        layout="inline"
        form={form}
        name="control-hooks3"
        onValuesChange={onValuesChange}
      >
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
          <InputNumber
            style={{ width: 250, marginTop: 10 }}
            placeholder="请输入节点个数"
          ></InputNumber>
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
                    shouldUpdate={(prevValues, curValues) =>
                      prevValues.area !== curValues.area ||
                      prevValues.sights !== curValues.sights
                    }
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
                      <Select
                        style={{ width: 138 }}
                        placeholder="请选择节点类型"
                      >
                        {specificationsNode.map((v) => (
                          <Select.Option
                            value={v.value}
                            key={v.value}
                            disabled={field.name === 1 && type === v.value}
                          >
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
                    <InputNumber
                      style={{ width: 250 }}
                      placeholder="请输入节点个数"
                    ></InputNumber>
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

// 升序、降序
export const OrderNode = (props: Props) => {
  const [dataSource, setDataSource] = React.useState([] as IOpClusterRoles[]);

  const handleOrder = (index1, index2) => {
    setDataSource(swapArr(index1, index2));
    props?.onChange(swapArr(index1, index2));
  };

  React.useEffect(() => {
    if (!props.id) return;
    getPhysicClusterRoles(props.id).then((data: IOpClusterRoles[]) => {
      data =
        data.map((ele, index) => {
          return {
            ...ele,
            label: ele.roleClusterName,
            value: ele.roleClusterName,
            key: index,
          };
        }) || [];
      setDataSource(data);
      props?.onChange(data);
    });
  }, []);

  const swapArr = (index1, index2) => {
    /*数组两个元素位置互换*/
    const array = [...dataSource];
    array.splice(index2, 1, ...array.splice(index1, 1, array[index2]));
    return array;
  };

  const columns = [
    {
      title: "排序",
      dataIndex: "id",
      key: "id",
      width: "20%",
    },
    {
      title: "节点名称",
      dataIndex: "roleClusterName",
      key: "roleClusterName",
      width: "60%",
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: (id: number, record, index) => {
        const ascendingOrder = (
          <a onClick={() => handleOrder(index, index - 1)}>
            <ArrowUpOutlined />
            升序
          </a>
        );
        const descendingOrder = (
          <a onClick={() => handleOrder(index, index + 1)}>
            <ArrowDownOutlined />
            降序
          </a>
        );
        const allOrder = (
          <div className="operation-box">
            <a onClick={() => handleOrder(index, index - 1)}>
              <ArrowUpOutlined />
              升序
            </a>{" "}
            <span className="line-between"></span>{" "}
            <a onClick={() => handleOrder(index, index + 1)}>
              <ArrowDownOutlined />
              降序
            </a>
          </div>
        );
        let operation: any = "-";
        if (dataSource?.length <= 1) {
          return <>{operation}</>;
        }
        if (index === 0) {
          operation = descendingOrder;
        } else if (index === dataSource.length - 1) {
          operation = ascendingOrder;
        } else {
          operation = allOrder;
        }
        return <>{operation}</>;
      },
    },
  ];

  return (
    <Table
      dataSource={dataSource}
      columns={columns}
      rowKey={"id"}
      pagination={false}
      scroll={{ y: 150 }}
    />
  );
};

// 扩缩容节点列表
export const ExpandShrinkNodeList = (props: Props) => {
  const onChange = (e) => {
    props.onChange(e.target.value);
  };

  const hostList = props.host?.split("\n") || [];
  return (
    <>
      {hostList.map((item) => (
        <>
          <span>{item}</span>
          <br />
        </>
      ))}
      {
        !props.isHidden ? 
          <TextArea
            rows={4}
            onChange={onChange}
            placeholder={"请输入主机列表，多个主机换行"}
          />
          : ""
      }
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
      <span style={{ paddingLeft: 10 }}>{`${
        isExpect ? "增加" : "减少"
      } ${Math.abs(props.podNumber - InputValue)}节点，${
        isExpect ? "扩容" : "缩容"
      }至 ${InputValue}`}</span>
    </>
  );
};
