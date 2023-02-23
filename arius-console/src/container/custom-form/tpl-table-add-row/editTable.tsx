import React, { useState } from "react";
import Table from "antd/lib/table";
import "./index.less";
import { v4 as uuidv4 } from "uuid";
import { cloneDeep } from "lodash";
import { Form } from "antd";
import { FormItemType, IFormItem, renderFormItem } from "component/x-form";
import { IState, XFormContext } from "./TableFormAddRow";
import { regIp } from "constants/reg";

const basicClass = "tpl-table-form-edittable";

const cpuOptions = [
  {
    value: "4核",
  },
  {
    value: "8核",
  },
  {
    value: "16核",
  },
  {
    value: "24核",
  },
];

const memoryOptions = [
  {
    value: "16G",
  },
  {
    value: "32G",
  },
  {
    value: "64G",
  },
];

const diskTypeOptions = [
  {
    value: "S2D",
  },
  {
    value: "VHD",
  },
];

const diskSizeOptions = [
  {
    value: "512g",
  },
  {
    value: "768g",
  },
  {
    value: "1024g",
  },
  {
    value: "1536g",
  },
  {
    value: "2048g",
  },
  {
    value: "2560g",
  },
  {
    value: "3072g",
  },
];

export const addRoleformMap: IFormItem[] = [
  {
    key: "host",
    label: "IP: 端口号",
    type: FormItemType.input,
    rules: [
      {
        required: true,
        validator: (rule: any, value: string) => {
          if ((rule.field.indexOf("datanode") !== -1 || rule.field.indexOf("clientnode") !== -1) && !value) {
            return Promise.resolve();
          }
          if (!value) {
            return Promise.reject("请输入IP:端口号，例如：127.1.1.1:8888");
          }
          const allValues = (window as any).formData?.allValues || {};
          const ipArr = [];
          const key = rule.field;
          Object.keys(allValues).forEach((item) => {
            const analysisKey = key?.split("&");
            if (analysisKey.length !== 3 || item === key) return;
            if (item.indexOf(`${analysisKey[0]}&${analysisKey[1]}`) > -1) {
              ipArr.push(allValues[item]);
            }
          });
          const isPostArr = value?.split(":");
          let judgeIp = false;
          ipArr.length &&
            ipArr.forEach((ipPort) => {
              const ip = ipPort?.split(":")[0];
              if (ip === isPostArr[0]) {
                judgeIp = true;
              }
            });
          if (judgeIp) {
            return Promise.reject("ip不能相同");
          }
          if (!new RegExp(regIp).test(isPostArr[0])) {
            return Promise.reject("请输入正确端ip，例：127.1.1.1:8888");
          }
          if (!isPostArr[1] && isPostArr[0]) {
            return Promise.reject("格式错误:号后面没有端口。");
          }
          return Promise.resolve();
        },
      },
    ],
    attrs: {
      placeholder: "请输入",
    },
  },
  {
    key: "cpu",
    label: "CPU核数",
    type: FormItemType.select,
    rules: [
      {
        required: true,
        message: "请选择",
        validator: (rule: any, value: string) => {
          if ((rule.field.indexOf("datanode") !== -1 || rule.field.indexOf("clientnode") !== -1) && !value) {
            return Promise.resolve();
          }
          if (!value) {
            return Promise.reject();
          }
          return Promise.resolve();
        },
      },
    ],
    options: cpuOptions,
  },
  {
    key: "memory",
    label: "内存大小",
    type: FormItemType.select,
    rules: [
      {
        required: true,
        message: "请选择",
        validator: (rule: any, value: string) => {
          if ((rule.field.indexOf("datanode") !== -1 || rule.field.indexOf("clientnode") !== -1) && !value) {
            return Promise.resolve();
          }
          if (!value) {
            return Promise.reject();
          }
          return Promise.resolve();
        },
      },
    ],
    options: memoryOptions,
  },
  {
    key: "diskType",
    label: "磁盘类型",
    type: FormItemType.select,
    rules: [
      {
        required: true,
        message: "请选择",
        validator: (rule: any, value: string) => {
          if ((rule.field.indexOf("datanode") !== -1 || rule.field.indexOf("clientnode") !== -1) && !value) {
            return Promise.resolve();
          }
          if (!value) {
            return Promise.reject();
          }
          return Promise.resolve();
        },
      },
    ],
    options: diskTypeOptions,
  },
  {
    key: "diskSize",
    label: "磁盘大小",
    type: FormItemType.select,
    rules: [
      {
        required: true,
        message: "请选择",
        validator: (rule: any, value: string) => {
          if ((rule.field.indexOf("datanode") !== -1 || rule.field.indexOf("clientnode") !== -1) && !value) {
            return Promise.resolve();
          }
          if (!value) {
            return Promise.reject();
          }
          return Promise.resolve();
        },
      },
    ],
    options: diskSizeOptions,
  },
];

export const EditTable = (props) => {
  const { state, dispatch } = React.useContext(XFormContext) as { state: IState; dispatch: any };
  const { type } = state;

  const [machineSpec, setMachineSpec] = useState("");

  const applyFormMap = [
    {
      key: "host",
      label: "IP: 端口号",
      type: FormItemType.input,
      rules: [
        {
          required: true,
          validator: (rule: any, value: string) => {
            if ((rule.field.indexOf("datanode") !== -1 || rule.field.indexOf("clientnode") !== -1) && !value) {
              return Promise.resolve();
            }
            if (!value) {
              return Promise.reject("请输入IP:端口号，例如：127.1.1.1:8888");
            }
            const allValues = (window as any).formData?.allValues || {};
            const ipArr = [];
            const key = rule.field;
            Object.keys(allValues).forEach((item) => {
              const analysisKey = key?.split("&");
              if (analysisKey.length !== 3 || item === key) return;
              if (item.indexOf(`${analysisKey[0]}&${analysisKey[1]}`) > -1) {
                ipArr.push(allValues[item]);
              }
            });
            const isPostArr = value?.split(":");
            let judgeIp = false;
            ipArr.length &&
              ipArr.forEach((ipPort) => {
                const ip = ipPort?.split(":")[0];
                if (ip === isPostArr[0]) {
                  judgeIp = true;
                }
              });
            if (judgeIp) {
              return Promise.reject("ip不能相同");
            }
            if (!new RegExp(regIp).test(isPostArr[0])) {
              return Promise.reject("请输入IP:端口号，例如：127.1.1.1:8888");
            }
            if (!isPostArr[1] && isPostArr[0]) {
              return Promise.reject('格式错误":"号后面没有端口');
            }
            return Promise.resolve();
          },
        },
      ],
      attrs: {
        placeholder: "请输入",
      },
    },
    {
      key: "machineSpec",
      label: "机型",
      type: FormItemType.select,
      rules: [
        {
          required: true,
          message: "请选择",
          validator: (rule: any, value: string) => {
            if ((rule.field.indexOf("datanode") !== -1 || rule.field.indexOf("clientnode") !== -1) && !value) {
              return Promise.resolve();
            }
            if (!value) {
              return Promise.reject();
            }
            setMachineSpec(value);
            return Promise.resolve();
          },
        },
      ],
      options: props.machineList,
    },
  ];

  const formMap = applyFormMap;

  const getColumns = () => {
    let length = state.dataSource.length;
    return [
      {
        dataIndex: formMap[0].key,
        title: formMap[0].label,
        width: 350,
        render: (text, record, index) => {
          const formItem = cloneDeep(formMap[0]);
          formItem.key = `${type}&${formItem.key}&${index}`;
          return (
            <div>
              <Form.Item
                className={`${basicClass}-table-formitem`}
                name={formItem.key}
                key={formItem.key}
                rules={
                  formItem.rules || [
                    {
                      required: false,
                      message: "",
                    },
                  ]
                }
              >
                {renderFormItem(formItem)}
              </Form.Item>
            </div>
          );
        },
      },
      {
        dataIndex: formMap[1].key,
        title: formMap[1].label,
        width: 300,
        render: (text, record, index) => {
          const formItem = cloneDeep(formMap[1]);
          formItem.key = `${type}&${formItem.key}&${index}`;
          return (
            <div>
              <Form.Item
                className={`${basicClass}-table-formitem`}
                name={formItem.key}
                key={formItem.key}
                initialValue={state.dataSource[length - 1]?.machineSpec}
                rules={
                  formItem.rules || [
                    {
                      required: false,
                      message: "",
                    },
                  ]
                }
              >
                {renderFormItem(formItem)}
              </Form.Item>
            </div>
          );
        },
      },
      {
        dataIndex: "option",
        title: "操作",
        width: 150,
        render: (_, record, index: number) => {
          return (
            <div>
              <svg
                onClick={() => {
                  dispatch({ key: "rowData", data: { machineSpec: machineSpec ? machineSpec : undefined } });
                }}
                className="icon svg-icon add-row"
                aria-hidden="true"
              >
                <use xlinkHref="#iconzengjia"></use>
              </svg>
              {state?.dataSource.length === 1 ? null : (
                <svg
                  onClick={() => {
                    dispatch({ key: "deleteRow", data: { index } });
                  }}
                  className="icon svg-icon delete-row"
                  aria-hidden="true"
                >
                  <use xlinkHref="#iconjianshao"></use>
                </svg>
              )}
            </div>
          );
        },
      },
    ];
  };

  return (
    <div className={basicClass}>
      <Table pagination={false} rowKey={() => uuidv4()} dataSource={state?.dataSource || []} columns={getColumns()} />
    </div>
  );
};

export default EditTable;
