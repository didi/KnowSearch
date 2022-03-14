import * as React from "react";
import { formatDate } from "../../Utils/tools";
import { renderTableLabels } from "../../ProTable/RenderTableLabels";
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

export const getFormCol = (deptItem, roleItem) => {
  return [
    {
      type: "input",
      title: "用户账号",
      dataIndex: "username",
      placeholder: "请输入用户账号",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "用户实名",
      dataIndex: "realName",
      placeholder: "请输入用户实名",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "custom",
      title: "所属部门",
      dataIndex: "deptId",
      ...deptItem,
    },
    {
      type: "select",
      title: "分配角色",
      dataIndex: "roleId",
      placeholder: "请输入角色名",
      ...roleItem,
    },
  ];
};

export const getFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

export const getTableCol = (renderIndex, renderUserNameCol, renderRealNameCol, renderOptCol) => {
  const columns = [
    {
      title: "序号",
      dataIndex: "index",
      key: "index",
      render: renderIndex,
    },
    {
      title: "用户账号",
      dataIndex: "username",
      key: "username",
      render: renderUserNameCol,
    },
    {
      title: "用户实名",
      dataIndex: "realName",
      key: "realName",
      render: renderRealNameCol,
    },
    {
      title: "所属部门",
      dataIndex: "deptList",
      key: "deptList",
      render: (list) => list.map((item) => item.deptName).join(">"),
    },
    {
      title: "电话",
      dataIndex: "phone",
      key: "phone",
    },
    {
      title: "邮箱",
      dataIndex: "email",
      key: "email",
    },
    {
      title: "分配角色",
      dataIndex: "roleList",
      key: "roleList",
      render: (value: any) =>
        renderTableLabels({
          list: value.map((item: any) => item && item.roleName),
          limit: 2,
        }),
    },
    {
      title: "最后更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      render: (value) => {
        return formatDate(value, "YYYY-MM-DD HH:mm:ss");
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: renderOptCol,
    },
  ];
  return columns;
};

export const readableForm = [
  {
    flag: ["detail", "update"],
    label: "用户账号/用户实名",
    prop: ["username", "realName"],
    readText: "",
  },
  {
    flag: ["detail", "update"],
    label: "部门",
    prop: "deptList",
    readText: "",
    render: (list) => {
      return list && list.map((item) => item.deptName).join(">");
    },
  },
  {
    flag: ["detail"],
    label: "绑定角色",
    prop: "roleList",
    readText: "",
    render: (list) => {
      return list && list.length > 0
        ? list.map((item: any) => (
            <span className="bind-row-item" key={item.id}>
              {item.roleName}
            </span>
          ))
        : "无";
    },
  },
];
