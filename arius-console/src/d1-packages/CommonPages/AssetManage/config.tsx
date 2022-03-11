import { IMenuItem } from "../../CommonComponents/HashMenu/hash-menu";
import { IColumnsType } from "../../ProForm/QueryForm/QueryForm";
import React from "react";
import { ResourcesTab } from "./resources-tab";
import { ResourceObj, TreeData, UserObj } from "./type";
import { UserTab } from "./user-tab";
import { CustomTreeSelect } from "./CustomFromItem/TreeSelect";

export enum TAB_LIST_KEY {
  resources = "resources",
  user = "user",
}

export const TAB_LIST = [
  {
    name: "按资源管理",
    key: TAB_LIST_KEY.resources,
    content: (ref: any) => <ResourcesTab ref={ref} />,
  },
  {
    name: "按用户管理",
    key: TAB_LIST_KEY.user,
    content: (ref: any) => <UserTab ref={ref} />,
  },
];

const menuMap = new Map<string, IMenuItem>();
TAB_LIST.forEach((d: any) => {
  menuMap.set(d.key, d);
});
export const MENU_MAP = menuMap;

export const queryFormText: { searchText: string; resetText: string } = {
  searchText: "查询",
  resetText: "重置",
};

const getDeptList = (childList, arr: { title: string; value: string | number }[]) => {
  childList.map(({ title, key, children }) => {
    arr.push({
      title,
      value: key,
    });
    if (children) {
      getDeptList(children, arr);
    }
  });
  console.log("arr", arr);
  return arr;
};

export const getUserTabQueryXForm = (data: TreeData): IColumnsType[] => {
  const formMap = [
    {
      dataIndex: "dept",
      title: "部门名称",
      type: "input",
      placeholder: "请输入",
      // component: <CustomTreeSelect treeData={data?.children} />, // 自定义树形选择
      // options: getDeptList(data?.children || [], []), // 下拉选择
    },
    {
      dataIndex: "uesrName",
      title: "用户账号",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "realName",
      title: "用户实名",
      type: "input",
      placeholder: "请输入",
    },
  ];
  return formMap;
};

export const getResourcesTabColumns = (onOpen: (record: ResourceObj) => any): any => {
  const columns = [
    {
      title: "项目ID",
      dataIndex: "projectCode",
      key: "projectCode",
    },
    {
      title: "项目名称",
      dataIndex: "projectName",
      key: "projectName",
    },
    {
      title: "资源类型",
      dataIndex: "resourceTypeName",
      key: "resourceTypeName",
    },
    {
      title: "归属项目",
      dataIndex: "projectName",
      key: "projectName1",
    },
    {
      title: "资源名称",
      dataIndex: "resourceName",
      key: "resourceName",
    },
    {
      title: "资源类型",
      dataIndex: "resourceTypeName",
      key: "resourceTypeName1",
    },
    {
      title: "管理权限用户数",
      dataIndex: "adminUserCnt",
      key: "adminUserCnt",
    },
    {
      title: "查看权限用户数",
      dataIndex: "viewUserCnt",
      key: "viewUserCnt",
      render: (viewUserCnt: unknown) => {
        return <>{viewUserCnt === null ? "不控制" : viewUserCnt}</>;
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: (id: number, record: ResourceObj) => {
        return <a onClick={() => onOpen(record)}>分配用户</a>;
      },
    },
  ];
  return columns;
};

export const getUserTabColumns = (onOpen: (record: UserObj) => any): any => {
  const columns = [
    {
      title: "用户账号",
      dataIndex: "username",
      key: "username",
    },
    {
      title: "用户实名",
      dataIndex: "realName",
      key: "realName",
    },
    {
      title: "部门",
      dataIndex: "deptList",
      key: "deptList",
      render: (deptList: { deptName: string }[]) => {
        return <>{deptList.map((item) => item.deptName).join("/")}</>;
      },
    },
    {
      title: "管理权限用户数",
      dataIndex: "adminResourceCnt",
      key: "adminResourceCnt",
    },
    {
      title: "查看权限用户数",
      dataIndex: "viewResourceCnt",
      key: "viewResourceCnt",
      render: (viewUserCnt: unknown) => {
        return <>{viewUserCnt === null ? "不控制" : viewUserCnt}</>;
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      render: (id: number, record: UserObj) => {
        return <a onClick={() => onOpen(record)}>分配资源</a>;
      },
    },
  ];
  return columns;
};
