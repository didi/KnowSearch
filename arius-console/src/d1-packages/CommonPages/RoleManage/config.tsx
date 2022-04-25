import { formatDate } from "../../Utils/tools";
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
      title: "角色ID",
      dataIndex: "roleCode",
      placeholder: "请输入角色ID",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "角色名称",
      dataIndex: "roleName",
      placeholder: "请输入角色名称",
      componentProps: {
        maxLength: 128,
      },
    },
    {
      type: "input",
      title: "描述信息",
      dataIndex: "description",
      placeholder: "请输入描述信息",
      componentProps: {
        maxLength: 128,
      },
    },
  ];
};

export const getTableCol = (renderIndex, renderRoleCodeCol, renderRoleNameCol: any, renderOptCol: any) => {
  const columns = [
    {
      title: "序号",
      dataIndex: "index",
      render: renderIndex,
    },
    {
      title: "角色ID",
      dataIndex: "roleCode",
      key: "roleCode",
      render: renderRoleCodeCol,
    },
    {
      title: "角色名称",
      dataIndex: "roleName",
      key: "roleName",
      render: renderRoleNameCol,
    },
    {
      title: "角色描述",
      dataIndex: "description",
      key: "description",
      width: "200px",
    },
    {
      title: "最后修改人",
      dataIndex: "lastReviser",
      key: "lastReviser",
    },
    {
      title: "分配用户数",
      dataIndex: "authedUserCnt",
      key: "authedUserCnt",
    },
    {
      title: "最后更新时间",
      dataIndex: "createTime",
      key: "createTime",
      render: (_value: string | number): any => {
        return formatDate(_value, "YYYY-MM-DD HH:mm:ss");
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
