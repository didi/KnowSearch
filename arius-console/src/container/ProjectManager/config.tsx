/* eslint-disable react/display-name */
import * as React from "react";
import { IMenuItem } from "interface/common";
import { NavRouterLink, renderAttributes, renderOperationBtns } from "container/custom-component";
import { timeFormat } from "constants/time";
import { deleteProject, getProject, checkResources } from "api";
import { message, Modal } from "antd";
import { ProjectPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { getCookie } from "lib/utils";
import "./index.less";
import { Utils } from "knowdesign";
const formatDate = Utils.formatDate;

export const getProjectColumns = (params: {
  setDrawerId: any;
  setModalId: any;
  reloadData?: any;
  isAdminUser: boolean;
  pagination?: any;
}) => {
  const { setDrawerId, setModalId, reloadData, isAdminUser, pagination } = params;
  const columns = [
    {
      title: "应用名称",
      dataIndex: "projectName",
      key: "projectName",
      width: 220,
      render: (text: number, record: any) => <NavRouterLink element={text} href={`/system/project/detail?projectId=${record.id}`} />,
    },
    {
      title: "责任人",
      dataIndex: "ownerList",
      key: "ownerList",
      width: 220,
      render: (text: any) => {
        return renderAttributes({ data: text.map((item) => item.userName), limit: 2 });
      },
    },
    {
      title: "成员数",
      dataIndex: "userList",
      key: "userList",
      width: 120,
      render: (text: any, record: any) => {
        let isAdmin = getCookie("isAdminUser");
        return <>{isAdmin === "yes" ? record?.userListWithBelongProjectAndAdminRole.length : text?.length}</>;
      },
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: 180,
      render: (text: string) => {
        return formatDate(text, timeFormat);
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      key: "operation",
      width: 150,
      render: (text: string, record: any) => {
        const btns = [
          {
            invisible: !hasOpPermission(ProjectPermissions.PAGE, ProjectPermissions.EDIT),
            clickFunc: async () => {
              const info = await getProject(record.id);
              const configInfo = info?.config || {};
              setDrawerId("addOrEditProjectModal", { ...record, ...info, ...configInfo }, reloadData);
            },
            label: "编辑",
          },
          {
            invisible: record.isAdmin || !hasOpPermission(ProjectPermissions.PAGE, ProjectPermissions.DELETE),
            clickFunc: async () => {
              setModalId("deleteProject", { ...record, pagination }, reloadData);
            },
            label: "删除",
          },
          {
            invisible: !isAdminUser,
            clickFunc: () => setDrawerId("AccessSetting", record),
            label: "访问设置",
          },
        ];
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getMemberListColumns = () => {
  const columns = [
    {
      title: "用户账号",
      dataIndex: "userName",
      key: "userName",
    },
    {
      title: "用户实名",
      dataIndex: "realName",
      key: "realName",
    },
    {
      title: "邮箱",
      dataIndex: "email",
      key: "email",
    },
    {
      title: "电话",
      dataIndex: "phone",
      key: "phone",
    },
    {
      title: "角色",
      dataIndex: "roleList",
      key: "roleList",
      render: (text: any) => {
        return <>{text?.join(",") || "-"}</>;
      },
    },
  ];
  return columns;
};

export const SEARCH_TYPE_MAP = {
  0: "集群模式",
  1: "索引模式",
  2: "原生模式",
};
export const getAccessSettingColumns = () => {
  const columns = [
    {
      title: "ES_User",
      dataIndex: "id",
      key: "id",
    },
    {
      title: "检验码",
      dataIndex: "verifyCode",
      key: "verifyCode",
    },
    {
      title: "访问模式",
      dataIndex: "searchType",
      key: "searchType",
      render: (text: number) => {
        return <>{SEARCH_TYPE_MAP[text]}</>;
      },
    },
    {
      title: "查询限流值",
      dataIndex: "queryThreshold",
      key: "queryThreshold",
    },
    {
      title: "访问集群",
      dataIndex: "cluster",
      key: "cluster",
      render: (val: string) => val || "-",
    },
  ];
  return columns;
};

export const getResourcesListColumns = (setModalId: any, reloadList?: any) => {
  const columns = [
    {
      title: "资源名",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "资源类型",
      dataIndex: "type",
      key: "type",
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      key: "operation",
      render: (text: string, record: any) => {
        const btns = [
          {
            clickFunc: () => setModalId("transferOfResources", record, reloadList),
            label: "转让",
          },
        ];
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getResourcesAssociatedListColumns = (setModalId: any, reloadList?: any) => {
  const columns = [
    {
      title: "资源名",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "资源类型",
      dataIndex: "type",
      key: "type",
    },
    {
      title: "细分权限",
      dataIndex: "auth",
      key: "auth",
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      key: "operation",
      render: (text: string, record: any) => {
        const btns = [
          {
            clickFunc: () => setModalId("resourcesAssociated", record, reloadList),
            label: "编辑细分权限",
          },
          {
            clickFunc: () => setModalId("transferOfResources", record, reloadList),
            label: "解绑",
          },
        ];
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return columns;
};

export const getProjectQueryXForm = () => {
  return [
    {
      type: "input",
      title: "应用名称:",
      dataIndex: "projectName",
      placeholder: "请输入应用名称",
    },
  ];
};

export const baseInfo = (projectInfo) => {
  return [
    {
      label: "责任人",
      key: "ownerList",
      render: (text: any) => {
        const names = [];
        text?.map((item) => {
          names.push(item.userName);
        });
        return <span className="project-detail-owner">{names?.join("，") || "-"}</span>;
      },
    },
    {
      label: "成员数",
      key: "userList",
      render: (text: any) => {
        let isAdmin = getCookie("isAdminUser");
        return <>{isAdmin === "yes" ? projectInfo?.userListWithBelongProjectAndAdminRole?.length : text?.length}</>;
      },
    },
    {
      label: "慢查询耗时",
      key: "slowQueryTimes",
    },
    {
      label: "创建时间",
      key: "createTime",
      render: (text: string) => {
        return formatDate(text, timeFormat);
      },
    },
    {
      label: "描述",
      key: "description",
    },
  ];
};

export enum DETAIL_TAB_KEY {
  members = "members",
  access = "access",
  resources = "resources",
  resourcesAssociated = "resourcesAssociated",
}

export const DETAIL_MENU = [
  {
    label: "成员列表",
    key: DETAIL_TAB_KEY.members,
  },
  {
    label: "访问设置",
    key: DETAIL_TAB_KEY.access,
  },
] as IMenuItem[];

const menuMap = new Map<string, IMenuItem>();
DETAIL_MENU.forEach((d) => {
  menuMap.set(d.key, d);
});

export const DETAIL_MENU_MAP = menuMap;
