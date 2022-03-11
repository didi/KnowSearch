import * as React from "react";
import { Cascader, Button, Tooltip, Popconfirm, Divider, DatePicker, Input } from "antd";
import { formatDate } from "../../Utils/tools";
import { renderTableLabels } from "../../ProTable/RenderTableLabels";
import { CheckCircleFilled, MinusCircleFilled } from "@ant-design/icons";
const { RangePicker } = DatePicker;
export interface ITableBtn {
  clickFunc?: () => void;
  type?: string;
  customFormItem?: string | JSX.Element;
  isRouterNav?: boolean;
  label: string | JSX.Element;
  className?: string;
  needConfirm?: boolean;
  aHref?: string;
  confirmText?: string;
  noRefresh?: boolean;
  loading?: boolean;
  disabled?: boolean;
  invisible?: boolean; // 不可见
}

export const getProjectQueryXForm = () => {
  return [
    {
      type: "input",
      title: "IP",
      dataIndex: "operatorIp",
      placeholder: "请输入项目ID",
      componentProps: {
        maxLength: 128,
      },
      submit: (e) => console.log(e),
    },
    {
      type: "input",
      title: "账号",
      dataIndex: "operatorUsername",
      placeholder: "请输入项目名称",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "select",
      title: "操作类型",
      dataIndex: "operateType",
      options: [
        {
          title: "新增",
          value: "",
        },
        {
          title: "编辑",
          value: 1,
        },
        {
          title: "删除",
          value: 2,
        },
        {
          title: "查看",
          value: 2,
        },
        {
          title: "下线",
          value: 2,
        },
        {
          title: "启动",
          value: 2,
        },
        {
          title: "停止",
          value: 2,
        },
        {
          title: "启用",
          value: 2,
        },
        {
          title: "停用",
          value: 2,
        },
      ],
    },
    {
      type: "select",
      title: "对象类型",
      dataIndex: "targetType",
      options: [
        {
          title: "新增",
          value: "",
        },
        {
          title: "编辑",
          value: 1,
        },
        {
          title: "删除",
          value: 2,
        },
        {
          title: "查看",
          value: 2,
        },
        {
          title: "下线",
          value: 2,
        },
        {
          title: "启动",
          value: 2,
        },
        {
          title: "停止",
          value: 2,
        },
        {
          title: "启用",
          value: 2,
        },
        {
          title: "停用",
          value: 2,
        },
      ],
    },
    {
      type: "input",
      title: "操作对象",
      dataIndex: "target",
      placeholder: "请输入项目名称",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "custom",
      title: "时间",
      dataIndex: "date",
      component: <RangePicker />,
    },
  ];
};

export const getFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

export const getProjectColumns = () => {
  const columns = [
    {
      title: "使用部门",
      dataIndex: "dept",
      key: "dept",
    },
    {
      title: "项目负责人",
      dataIndex: "chargeUser",
      key: "chargeUser",
      render: (_value: any, _row: { chargeUserIdList: [] }) =>
        renderTableLabels({
          list: _row.chargeUserIdList,
          limit: 2,
        }),
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
    },
    {
      title: "状态",
      dataIndex: "isRunning",
      key: "isRunning",
      // eslint-disable-next-line react/display-name
      render: (_value: boolean) => {
        return (
          <span>
            {_value ? (
              <>
                <CheckCircleFilled style={{ color: "#46D677", marginRight: "4px" }} />
                <span>启用</span>
              </>
            ) : (
              <>
                <MinusCircleFilled style={{ color: "#F4A838", marginRight: "4px" }} />
                <span>禁用</span>
              </>
            )}
          </span>
        );
      },
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      render: (_value: any) => {
        formatDate(_value, "YYYY-MM-DD HH:mm:ss");
      },
    },
  ];
  return columns;
};

export const readableForm = [
  {
    readonly: true,
    label: "IP",
    prop: "operatorIp",
    readText: "",
  },
  {
    readonly: true,
    label: "账号",
    prop: "operatorUsername",
    readText: "",
  },
  {
    readonly: true,
    label: "时间",
    prop: "createTime",
    readText: "",
  },
  {
    readonly: true,
    label: "操作类型",
    prop: "operateType",
    readText: "",
  },
  {
    readonly: true,
    label: "对象分类",
    prop: "targetType",
    readText: "",
  },
  {
    readonly: true,
    label: "操作类型",
    prop: "target",
    readText: "",
  },
];
export const writableForm = [
  {
    label: "项目名称:",
    name: "projectName",
    formProps: {
      maxLength: 128,
    },
    itemProps: {
      rules: [{ required: true, message: "项目名称不能为空" }],
    },
  },
  {
    label: "使用部门:",
    name: "dept",
    itemProps: {
      rules: [{ required: true, message: "使用部门不能为空" }],
    },
  },
  {
    label: "负责人:",
    name: "chargeUserId",
    itemProps: {
      rules: [{ required: true, message: "负责人不能为空" }],
    },
  },
  {
    type: "textarea",
    label: "描述:",
    name: "description",
    itemProps: {
      rules: [{ required: true, message: "描述不能为空" }],
    },
    formProps: {
      maxLength: 512,
    },
  },
  {
    type: "radio",
    label: "状态:",
    name: "isRunning",
    submit: (e) => console.log(e),

    itemProps: {
      rules: [{ required: true, message: "状态不能为空" }],
    },
    options: [
      {
        label: "是",
        value: true,
      },
      {
        label: "否",
        value: false,
      },
    ],
  },
];
