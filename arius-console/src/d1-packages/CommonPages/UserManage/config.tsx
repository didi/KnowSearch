import * as React from "react";
import { formatDate } from "knowdesign/lib/utils/tools";
import { renderAttributes } from "container/custom-component";
import { Tooltip } from "antd";
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
      title: "用户账号:",
      dataIndex: "userName",
      placeholder: "请输入用户账号",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "用户实名:",
      dataIndex: "realName",
      placeholder: "请输入用户实名",
      componentProps: {
        maxLength: 128,
      },
    },
  ];
};

export const getFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

export const getTableCol = (renderIndex, renderUserNameCol, renderOptCol) => {
  const columns = [
    {
      title: "序号",
      dataIndex: "index",
      key: "index",
      render: renderIndex,
    },
    {
      title: "用户账号",
      dataIndex: "userName",
      key: "userName",
      render: renderUserNameCol,
    },
    {
      title: "用户实名",
      dataIndex: "realName",
      key: "realName",
      render: renderUserNameCol,
    },
    {
      title: "所属应用",
      dataIndex: "projectList",
      key: "projectList",
      render: (list) =>
        renderAttributes({ data: list?.map((item: any) => item && item.projectName) || [], limit: 2, placement: "bottomLeft" }),
    },
    {
      title: "电话",
      dataIndex: "phone",
      key: "phone",
      render: (text: string) => {
        return <>{text || "-"}</>;
      },
    },
    {
      title: "邮箱",
      dataIndex: "email",
      key: "email",
      render: (text: string) => {
        return <>{text || "-"}</>;
      },
    },
    {
      title: "分配角色",
      dataIndex: "roleList",
      key: "roleList",
      render: (value: any) =>
        renderAttributes({ data: value?.map((item: any) => item && item.roleName) || [], limit: 2, placement: "bottomLeft" }),
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
      filterTitle: true,
      key: "operation",
      width: 150,
      render: renderOptCol,
    },
  ];
  return columns;
};

const columnsRender = (item: string, maxWidth) => {
  return (
    <Tooltip placement="right" title={item}>
      <div
        className="row-ellipsis"
        style={{
          maxWidth,
          display: "inline-block",
        }}
      >
        {item || (typeof item === "number" ? item : "-")}
      </div>
    </Tooltip>
  );
};

export const readableForm = [
  {
    flag: ["detail", "update"],
    label: "已选用户",
    prop: ["userName", "realName"],
    readText: "",
  },
  {
    flag: ["detail"],
    label: "密码",
    prop: "password",
    readText: "",
    render: (text) => text || "-",
  },
  {
    flag: ["detail", "update"],
    label: "所属应用",
    prop: "projectList",
    readText: "",
    render: (list) => {
      return columnsRender(list?.map((item: any) => item && item.projectName)?.join("；") || "-", "180px");
      // return renderAttributes({ data: list?.map((item: any) => item && item.projectName) || [], limit: 2, placement: "bottomLeft" });
    },
  },
  {
    flag: ["detail"],
    label: "绑定角色",
    prop: "roleList",
    readText: "",
    render: (list) => {
      return columnsRender(list?.map((item: any) => item && item.roleName)?.join("；") || "-", "210px");
      // return renderAttributes({ data: list?.map((item: any) => item && item.roleName) || [], limit: 6, placement: "bottomLeft" });
    },
  },
];
