import React from "react";
import { Utils } from "knowdesign";
const formatDate = Utils.formatDate;
import { regNonnegativeInteger } from "constants/reg";

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

export const getFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

export const getFormCol = () => {
  return [
    {
      type: "input",
      title: "角色ID:",
      dataIndex: "id",
      placeholder: "请输入角色ID",
      componentProps: {
        maxLength: 128,
      },
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
            }
            if (value && value?.length > 16) {
              return Promise.reject(new Error("请输入正确ID，0-16位字符"));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      type: "input",
      title: "角色名称:",
      dataIndex: "roleName",
      placeholder: "请输入角色名称",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "描述:",
      dataIndex: "description",
      placeholder: "请输入描述",
      componentProps: {
        maxLength: 128,
      },
    },
  ];
};

export const getTableCol = (renderIndex, renderRoleDetail, renderUserNum, renderOptCol) => {
  const columns = [
    {
      title: "序号",
      dataIndex: "index",
      render: renderIndex,
    },
    {
      title: "角色ID",
      dataIndex: "id",
      key: "id",
      render: renderRoleDetail,
    },
    {
      title: "角色名称",
      dataIndex: "roleName",
      key: "roleName",
      render: renderRoleDetail,
    },
    {
      title: "角色描述",
      dataIndex: "description",
      key: "description",
      width: "200px",
      lineClampTwo: true,
      needTooltip: true,
      render: (text: string) => text || "-",
    },
    {
      title: "绑定用户数",
      dataIndex: "authedUserCnt",
      key: "authedUserCnt",
      render: renderUserNum,
    },
    {
      title: "最后修改人",
      dataIndex: "lastReviser",
      key: "lastReviser",
      render: (text: string) => text || "-",
    },
    {
      title: "最后更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      render: (_value: string | number): any => {
        return formatDate(_value, "YYYY-MM-DD HH:mm:ss");
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      width: 180,
      key: "operation",
      render: renderOptCol,
    },
  ];
  return columns;
};

export const readableForm = [
  {
    label: "角色名称",
    prop: "roleName",
    readText: "",
    render: null,
  },
  {
    label: "描述",
    prop: "description",
    readText: "",
    render: null,
  },
];
