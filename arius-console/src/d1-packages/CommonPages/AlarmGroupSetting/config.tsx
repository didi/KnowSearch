import * as React from "react";
import { formatDate } from "../../Utils/tools";
import { renderTableLabels } from "../../ProTable/RenderTableLabels";
import { CheckCircleFilled, MinusCircleFilled } from "@ant-design/icons";
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

export const getFormCol = () => {
  return [
    {
      type: "input",
      title: "告警组名称",
      dataIndex: "name",
      placeholder: "请输入告警组名称",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "告警组成员",
      dataIndex: "member",
      placeholder: "请输入用户名",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "最后修改人",
      dataIndex: "operator",
      placeholder: "请输入用户名",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "select",
      title: "状态",
      dataIndex: "status",
      placeholder: "请选择状态",
      options: [
        {
          value: "",
          title: "全部",
        },
        {
          value: 1,
          title: "启用",
        },
        {
          value: 0,
          title: "停用",
        },
      ],
    },
  ];
};

export const getFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

export const getTableCol = (renderIndex, renderNameCol, renderOptsCol) => {
  return [
    {
      title: "序号",
      dataIndex: "index",
      key: "index",
      render: renderIndex,
    },
    {
      title: "告警组名称",
      dataIndex: "name",
      key: "name",
      render: renderNameCol,
    },
    {
      title: "告警组成员",
      dataIndex: "userList",
      key: "userList",
      render: (_value: any) =>
        renderTableLabels({
          list: _value.map((item) => item.name),
          limit: 2,
        }),
    },
    {
      title: "描述",
      dataIndex: "comment",
      key: "comment",
    },
    {
      title: "最后修改人",
      dataIndex: "operator",
      key: "operator",
    },
    {
      title: "最后更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      render: (_value: any) => formatDate(_value, "YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (_value: any) => {
        return (
          <span>
            {_value === 1 && (
              <>
                <CheckCircleFilled style={{ color: "#46D677", marginRight: "4px" }} />
                <span>启用</span>
              </>
            )}
            {_value === 0 && (
              <>
                <MinusCircleFilled style={{ color: "#F4A838", marginRight: "4px" }} />
                <span>停用</span>
              </>
            )}
          </span>
        );
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: renderOptsCol,
    },
  ];
};

export const readableForm = [
  {
    readonly: true,
    label: "告警组名称",
    prop: "name",
    readText: "",
  },
  {
    readonly: true,
    label: "告警组成员",
    prop: "members",
    readText: "",
    className: "user-list",
    render: (list) => {
      return (
        list &&
        list.map((item: any) => (
          <span className="user-item" key={item.id}>
            {item.name}
          </span>
        ))
      );
    },
  },
  {
    readonly: true,
    label: "描述",
    prop: "comment",
    readText: "",
  },
  {
    readonly: true,
    label: "状态",
    prop: "status",
    readText: "",
    render: (value) => {
      return value === 1 ? (
        <>
          <CheckCircleFilled style={{ color: "#46D677", marginRight: "4px" }} />
          <span>启用</span>
        </>
      ) : (
        <>
          <MinusCircleFilled style={{ color: "#F4A838", marginRight: "4px" }} />
          <span>停用</span>
        </>
      );
    },
  },
];
