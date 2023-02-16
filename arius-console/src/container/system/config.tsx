import React from "react";
import { IDeploy, IOpRecord } from "typesPath/cluster/physics-type";
import { Tooltip, Modal, DatePicker, Button } from "antd";
import { cellStyle } from "constants/table";
import { deployStatus } from "constants/status-map";
import { renderOperationBtns } from "container/custom-component";
import { QuestionCircleOutlined } from "@ant-design/icons";
import { deleteDeploy, switchDeploy } from "api/cluster-api";
import { PlatformPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { transTimeFormat } from "lib/utils";
import { XNotification } from "component/x-notification";
import DRangeTime from "../../d1-packages/d-range-time";
const { confirm } = Modal;
const { RangePicker } = DatePicker;

export const getClusterCongigQueryXForm = () => {
  const formMap = [
    {
      dataIndex: "valueGroup",
      title: "配置组",
      type: "input",
      placeholder: "请输入配置组",
    },
    {
      dataIndex: "valueName",
      title: "名称",
      type: "input",
      placeholder: "请输入名称",
    },
    {
      dataIndex: "memo",
      title: "描述",
      type: "input",
      placeholder: "请输入描述",
    },
  ];
  return formMap;
};

export const getOperatingListQueryXForm = (params, handleTimeChange) => {
  const customTimeOptions = [
    {
      label: "最近 1 天",
      value: 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 7 天",
      value: 7 * 24 * 60 * 60 * 1000,
    },
    {
      label: "最近 1 月",
      value: 30 * 24 * 60 * 60 * 1000,
    },
  ];
  const formMap = [
    {
      dataIndex: "projectName",
      title: "应用",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "moduleId",
      title: "模块",
      type: "select",
      options: params.modules,
      placeholder: "请选择",
    },
    {
      dataIndex: "operateId",
      title: "操作类型",
      type: "select",
      options: params.operationType,
      placeholder: "请选择",
    },
    {
      dataIndex: "userOperation",
      title: "操作人",
      type: "input",
      placeholder: "请输入",
    },
    {
      dataIndex: "triggerWayId",
      title: "触发方式",
      type: "select",
      options: params.triggerWay,
      placeholder: "请选择",
    },
    {
      dataIndex: "operateTime",
      title: "操作时间",
      type: "custom",
      component: <DRangeTime timeChange={handleTimeChange} popoverClassName="dashborad-popover" customTimeOptions={customTimeOptions} />,
    },
    {
      dataIndex: "content",
      title: "操作内容",
      type: "input",
      placeholder: "请输入",
    },
  ];
  return formMap;
};

export const getClusterCongigColumns = (data: IDeploy[], fn: any, reloadDataFn: any) => {
  const getClusterCongigBtnList = (record: IDeploy, fn: any, reloadDataFn: any) => {
    const title = record.status === 1 ? "禁用" : "开启";
    return [
      // {
      //   label: title,
      //   invisible: !hasOpPermission(PlatformPermissions.PAGE, PlatformPermissions.DISABLE),
      //   clickFunc: () => {
      //     const { id, status } = record;
      //     confirm({
      //       title: "提示",
      //       icon: <QuestionCircleOutlined style={{ color: "red" }} />,
      //       content: `确定${title}配置${id}？`,
      //       width: 500,
      //       okText: "确认",
      //       cancelText: "取消",
      //       onOk() {
      //         switchDeploy({ id, status: status === 1 ? 2 : 1 }).then((data) => {
      //           XNotification({ type: "success", message: `${title}配置成功` });
      //           reloadDataFn();
      //         });
      //       },
      //     });
      //   },
      // },
      {
        label: "编辑",
        invisible: !hasOpPermission(PlatformPermissions.PAGE, PlatformPermissions.EDIT),
        clickFunc: () => {
          fn("clusterConfigModal", record, reloadDataFn);
        },
      },
      {
        label: "删除",
        invisible: !hasOpPermission(PlatformPermissions.PAGE, PlatformPermissions.DELETE),
        clickFunc: (record: IDeploy) => {
          const { id } = record;
          confirm({
            title: "提示",
            icon: <QuestionCircleOutlined style={{ color: "red" }} />,
            content: `确定删除配置${id}？`,
            width: 500,
            okText: "确认",
            cancelText: "取消",
            onOk() {
              deleteDeploy(id).then((data) => {
                XNotification({ type: "success", message: "删除成功" });
                reloadDataFn();
              });
            },
          });
          return;
        },
      },
    ];
  };
  const columns = [
    {
      title: "ID",
      dataIndex: "id",
      width: 80,
      sorter: (a: IDeploy, b: IDeploy) => a.id - b.id,
    },
    {
      title: "配置组",
      dataIndex: "valueGroup",
      width: 150,
      onCell: () => ({
        style: {
          maxWidth: 250,
          ...cellStyle,
        },
      }),
      lineClampTwo: true,
      needTooltip: true,
      render: (value: string) => {
        return value || "-";
      },
    },
    {
      title: "名称",
      dataIndex: "valueName",
      width: 200,
      onCell: () => ({
        style: {
          maxWidth: 200,
          ...cellStyle,
        },
      }),
      lineClampTwo: true,
      needTooltip: true,
      render: (value: string) => {
        return value || "-";
      },
    },
    {
      title: "值",
      dataIndex: "value",
      width: 180,
      onCell: () => ({
        style: {
          maxWidth: 250,
          ...cellStyle,
        },
      }),
      lineClampTwo: true,
      needTooltip: true,
      render: (value: string) => {
        return value || "-";
      },
    },
    {
      title: "描述",
      dataIndex: "memo",
      width: 200,
      onCell: () => ({
        style: {
          maxWidth: 250,
          ...cellStyle,
        },
      }),
      lineClampTwo: true,
      needTooltip: true,
      render: (value: string) => {
        return value || "-";
      },
    },
    {
      title: "操作",
      dataIndex: "operation",
      filterTitle: true,
      width: 120,
      render: (id: number, record: IDeploy) => {
        const btns = getClusterCongigBtnList(record, fn, reloadDataFn);
        return renderOperationBtns(btns as any, record);
      },
    },
  ];
  return columns;
};

export const getOperationColumns = (setDrawerId) => {
  let cols = [
    {
      title: "应用",
      dataIndex: "projectName",
      key: "projectName",
      width: "10%",
      render: (val: string) => val || "-",
    },
    {
      title: "模块",
      dataIndex: "module",
      key: "module",
      width: "10%",
      render: (val: string) => val || "-",
    },
    {
      title: "操作类型",
      dataIndex: "operate",
      key: "operate",
      width: "15%",
      render: (val: string) => val || "-",
    },
    {
      title: "操作人",
      dataIndex: "userOperation",
      key: "userOperation",
      width: "10%",
      render: (val: string) => val || "-",
    },
    {
      title: "操作时间",
      dataIndex: "operateTime",
      key: "operateTime",
      width: 200,
      render: (t: number) => transTimeFormat(t),
    },
    {
      title: "触发方式",
      dataIndex: "triggerWay",
      key: "triggerWay",
      width: "10%",
      render: (val: string) => val || "-",
    },
    {
      title: "操作内容",
      dataIndex: "content",
      key: "content",
      width: 260,
      ellipsis: true,
      render: (val: string, record) => {
        let diffArr = ["编辑MAPPING", "编辑SETTING", "配置文件变更"];
        if (diffArr.includes(record.operate)) {
          const data = val ? JSON.parse(val) : {};
          let operate: string;
          if (record.operate === "编辑MAPPING") {
            operate = "Mapping";
          } else if (record.operate === "编辑SETTING") {
            operate = "Setting";
          } else if (record.operate === "配置文件变更") {
            operate = "配置文件";
          }
          return (
            <>
              <span style={{ marginRight: "5px" }}>{record.operate}</span>
              <Button
                type="link"
                style={{ height: 20, padding: "0 15px" }}
                onClick={() => setDrawerId("mappingSettingDiff", { operate, data })}
              >
                查看
              </Button>
            </>
          );
        }
        return (
          <Tooltip title={val || "-"} placement="topLeft">
            <span>{val || "-"}</span>
          </Tooltip>
        );
      },
    },
  ];
  return cols;
};
