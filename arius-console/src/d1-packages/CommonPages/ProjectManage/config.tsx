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

export const getProjectQueryXForm = (renderDeptTree) => {
  return [
    {
      type: "input",
      title: "项目ID",
      dataIndex: "projectCode",
      placeholder: "请输入项目ID",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "项目名称",
      dataIndex: "projectName",
      placeholder: "请输入项目名称",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "custom",
      title: "使用部门",
      dataIndex: "deptId",
      component: renderDeptTree(),
    },
    {
      type: "input",
      title: "负责人",
      dataIndex: "chargeUsername",
      placeholder: "请输入用户账号",
      initialValue: "all",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "select",
      title: "状态",
      dataIndex: "running",
      options: [
        {
          title: "全部",
          value: "",
        },
        {
          title: "启用",
          value: true,
        },
        {
          title: "停用",
          value: false,
        },
      ],
    },
  ];
};

export const getFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

export const getProjectColumns = (renderIndex, renderProjectCodeCol, renderProjectNameCol, renderOptsCol) => {
  const columns = [
    {
      title: "序号",
      dataIndex: "index",
      render: renderIndex,
    },
    {
      title: "项目ID",
      dataIndex: "projectCode",
      key: "projectCode",
      render: renderProjectCodeCol,
    },
    {
      title: "项目名",
      dataIndex: "projectName",
      key: "projectName	",
      render: renderProjectNameCol,
    },
    {
      title: "使用部门",
      dataIndex: "deptList",
      key: "deptList",
      render: (value) => value.map((item) => item.deptName).join("-"),
    },
    {
      title: "项目负责人",
      dataIndex: "userList",
      key: "userList",
      render: (value: any) =>
        renderTableLabels({
          list: value.map((item: any) => item && item.username),
          limit: 2,
        }),
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      width: "200px",
    },
    {
      title: "状态",
      dataIndex: "running",
      key: "running",
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
                <span>停用</span>
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
        return formatDate(_value, "YYYY-MM-DD HH:mm:ss");
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: renderOptsCol,
    },
  ];
  return columns;
};

export const readableForm = [
  {
    label: "项目名称",
    prop: "projectName",
    readText: "",
    className: "",
  },
  {
    label: "使用部门",
    prop: "deptList",
    readText: "",
    render: (list) => list && list.map((item) => item.deptName).join("-"),
  },
  {
    label: "负责人",
    prop: "userList",
    readText: "",
    className: "user-list",
    render: (list) => {
      return (
        list &&
        list.map((item: any) => (
          <span className="bind-user-item" key={item.id}>
            {item.username}
          </span>
        ))
      );
    },
  },
  {
    label: "描述",
    prop: "description",
    readText: "",
    className: "",
  },
  {
    label: "状态",
    prop: "running",
    readText: "",
    className: "",
    render: (val: any) => {
      return val ? (
        <>
          <CheckCircleFilled style={{ color: "#46D677", marginRight: "4px" }} />
          <span>启用</span>
        </>
      ) : (
        <>
          <MinusCircleFilled style={{ color: "#F4A838", marginRight: "4px" }} />
          <span>禁用</span>
        </>
      );
    },
  },
];
