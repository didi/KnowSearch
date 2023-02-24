import React from "react";
import { renderOperationBtns } from "container/custom-component";
import { hasOpPermission } from "lib/permission";
import { transTimeFormat } from "lib/utils";
import { XNotification } from "component/x-notification";
import { regNonnegativeInteger } from "constants/reg";
import { ScriptCenterPermissions, SoftwareCenterPermissions } from "constants/permission";
import { XModal } from "component/x-modal";
import { checkScriptUsing, delScript, getSoftware, checkSoftwareUsing, delSoftware, getScriptNameList } from "api/software-admin";
import { message } from "antd";
import "./index.less";

export const getScriptCenterXForm = () => {
  const formMap = [
    {
      dataIndex: "id",
      title: "脚本ID",
      type: "input",
      placeholder: "请输入脚本ID",
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
            }
            if (value?.length > 16) {
              return Promise.reject(new Error("请输入正确ID，0-16位字符"));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      dataIndex: "name",
      title: "脚本名称",
      type: "input",
      placeholder: "请输入脚本名称",
    },
  ];
  return formMap;
};

export const getScriptCenterColumns = (setDrawerId, reloadDataFn) => {
  const getOperationList = (record, setDrawerId, reloadDataFn) => {
    return [
      {
        invisible: !hasOpPermission(ScriptCenterPermissions.PAGE, ScriptCenterPermissions.EDIT),
        label: "编辑",
        clickFunc: async () => {
          setDrawerId("addScriptDrawer", record, reloadDataFn);
        },
      },
      {
        invisible: !hasOpPermission(ScriptCenterPermissions.PAGE, ScriptCenterPermissions.DELETE),
        label: "删除",
        clickFunc: async () => {
          let isUsing = await checkScriptUsing(record.id);
          if (isUsing) {
            XModal({
              type: "warning",
              title: `脚本${record?.name || ""}正在使用中，无法删除！`,
            });
            return;
          }
          XModal({
            type: "delete",
            title: `确定删除脚本${record?.name || ""}？`,
            onOk: async () => {
              await delScript(record.id);
              message.success("删除成功");
              reloadDataFn();
            },
          });
        },
      },
    ];
  };
  let cols = [
    {
      title: "脚本ID",
      dataIndex: "id",
      key: "id",
      width: 80,
      render: (val: string) => val || "-",
    },
    {
      title: "脚本名称",
      dataIndex: "name",
      key: "name",
      width: 100,
      lineClampTwo: true,
      needTooltip: true,
      render: (val: string) => val || "-",
    },
    {
      title: "模板ID",
      dataIndex: "templateId",
      key: "templateId",
      width: 80,
      render: (val: string) => val || "-",
    },
    {
      title: "模板超时时间",
      dataIndex: "timeout",
      key: "timeout",
      width: 80,
      render: (val: string) => val || "-",
    },
    {
      title: "url",
      dataIndex: "contentUrl",
      key: "contentUrl",
      lineClampTwo: true,
      needTooltip: true,
      width: 230,
      render: (val: string) => {
        const onClick = () => {
          if (!val) return;
          let aDom = document.createElement("a");
          aDom.style.display = "none";
          aDom.href = val;
          let file = val.split("/");
          let fileName = file[file.length - 1];
          aDom.setAttribute("download", fileName);
          document.body.appendChild(aDom);
          aDom.click();
          document.body.removeChild(aDom);
        };
        return (
          <div className="script-url" onClick={onClick}>
            {val || "-"}
          </div>
        );
      },
    },
    {
      title: "脚本描述",
      dataIndex: "describe",
      key: "describe",
      width: 150,
      render: (val: string) => val || "-",
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: 150,
      render: (t: number) => transTimeFormat(t),
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      width: 150,
      render: (t: number) => transTimeFormat(t),
    },
    {
      title: "创建者",
      dataIndex: "creator",
      key: "creator",
      width: 100,
      render: (val: string) => val || "-",
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      filterTitle: true,
      width: 120,
      render: (text: string, record: any) => {
        const btns = getOperationList(record, setDrawerId, reloadDataFn);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return cols;
};

export const getSoftwareCenterXForm = () => {
  const formMap = [
    {
      dataIndex: "id",
      title: "软件ID",
      type: "input",
      placeholder: "请输入软件ID",
      rules: [
        {
          required: false,
          validator: (rule: any, value: string) => {
            if (value && !new RegExp(regNonnegativeInteger).test(value)) {
              return Promise.reject(new Error("请输入正确格式"));
            }
            if (value?.length > 16) {
              return Promise.reject(new Error("请输入正确ID，0-16位字符"));
            }
            return Promise.resolve();
          },
        },
      ],
    },
    {
      dataIndex: "name",
      title: "软件名称",
      type: "input",
      placeholder: "请输入软件名称",
    },
  ];
  return formMap;
};

export const getSoftwareColumns = (setDrawerId, reloadDataFn) => {
  const getOperationList = (record, setDrawerId, reloadDataFn) => {
    return [
      {
        invisible: !hasOpPermission(SoftwareCenterPermissions.PAGE, SoftwareCenterPermissions.EDIT),
        label: "编辑",
        clickFunc: async () => {
          let data = await getSoftware(record.id);
          let res = await getScriptNameList();
          let scriptList = (res || []).map((item) => ({ value: item?.id, label: item?.name }));
          let params = { ...record, ...data, scriptList };
          setDrawerId("addSoftwareDrawer", params, reloadDataFn);
        },
      },
      {
        invisible: !hasOpPermission(SoftwareCenterPermissions.PAGE, SoftwareCenterPermissions.DELETE),
        label: "删除",
        clickFunc: async () => {
          let isUsing = await checkSoftwareUsing(record.id);
          if (isUsing) {
            XModal({
              type: "warning",
              title: `${record?.name || ""}正在使用中，无法删除！`,
            });
            return;
          }
          XModal({
            type: "delete",
            title: `确定删除${record?.name || ""}吗？`,
            onOk: async () => {
              await delSoftware(record.id);
              message.success("删除成功");
              reloadDataFn();
            },
          });
        },
      },
    ];
  };
  let cols = [
    {
      title: "软件ID",
      dataIndex: "id",
      key: "id",
      width: 80,
      render: (val: string) => val || "-",
    },
    {
      title: "软件名称",
      dataIndex: "name",
      key: "name",
      lineClampTwo: true,
      needTooltip: true,
      width: 120,
      render: (val: string) => val || "-",
    },
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
      width: 80,
      render: (val: string) => val || "-",
    },
    {
      title: "url",
      dataIndex: "url",
      key: "url",
      lineClampTwo: true,
      needTooltip: true,
      width: 230,
      render: (val: string) => {
        const onClick = () => {
          if (!val) return;
          let aDom = document.createElement("a");
          aDom.style.display = "none";
          aDom.href = val;
          let file = val.split("/");
          let fileName = file[file.length - 1];
          aDom.setAttribute("download", fileName);
          document.body.appendChild(aDom);
          aDom.click();
          document.body.removeChild(aDom);
        };
        return (
          <div className="script-url" onClick={onClick}>
            {val || "-"}
          </div>
        );
      },
    },
    {
      title: "创建时间",
      dataIndex: "createTime",
      key: "createTime",
      width: 130,
      render: (t: number) => transTimeFormat(t),
    },
    {
      title: "更新时间",
      dataIndex: "updateTime",
      key: "updateTime",
      width: 130,
      render: (t: number) => transTimeFormat(t),
    },
    {
      title: "创建者",
      dataIndex: "creator",
      key: "creator",
      width: 80,
      render: (val: string) => val || "-",
    },
    {
      title: "描述",
      dataIndex: "describe",
      key: "describe",
      width: 120,
      render: (val: string) => val || "-",
    },
    {
      title: "操作",
      dataIndex: "operation",
      key: "operation",
      filterTitle: true,
      width: 120,
      render: (text: string, record: any) => {
        const btns = getOperationList(record, setDrawerId, reloadDataFn);
        return renderOperationBtns(btns, record);
      },
    },
  ];
  return cols;
};
