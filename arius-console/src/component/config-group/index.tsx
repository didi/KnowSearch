import React, { useState, useEffect } from "react";
import { Radio, Input, Form, Tooltip, Checkbox } from "antd";
import { TableFormAddRow } from "container/custom-form/tpl-table-add-row/TableFormAddRow";
import { configTypeOptions } from "constants/status-map";
import { ApplyConfigGroupList } from "container/custom-form/apply-config-group-table";
import { InstallPluginTable } from "container/custom-form/install-plugin-table";
import { XNotification } from "component/x-notification";
import { EditConfigTab } from "./edit-config-tab";
import { uuid } from "lib/utils";
import "./index.less";

export const EditConfigGroup = (props) => {
  const {
    type,
    data,
    index = 0,
    configList,
    setConfigList,
    machineList,
    className,
    groupKey,
    plugType,
    addSoftware,
    defaultGroupNames,
    setDefaultGroupNames,
  } = props;
  const [activeRadioKey, setActiveRadioKey] = useState(1);

  useEffect(() => {
    if (!configList.length) {
      setConfigList([JSON.parse(JSON.stringify(newGroupData))]);
    }
  }, []);

  const newGroupData = {
    key: uuid(),
    groupName: "",
    fileConfig: [
      {
        label: "newFile",
        key: "newFile-1",
        value: "",
      },
    ],
    systemConfig: [
      {
        label: "systemConfig",
        key: "systemConfig-1",
        value: "",
      },
    ],
    runningConfig: [
      {
        label: "runningConfig",
        key: "runningConfig-1",
        value: "",
      },
    ],
    ipList: [],
    hosts: [], // 节点列表
  };

  const onAddConfig = () => {
    let newConfigList = [...configList];
    newConfigList.splice(index + 1, 0, newGroupData);
    setConfigList(newConfigList);
  };

  const onDeleteConfig = () => {
    if (type === "installPlugin") {
      if (data?.hosts?.length) {
        XNotification({ type: "error", message: "请移除IP后再删除配置组" });
        return;
      }
    }
    let newConfigList = [...configList];
    newConfigList.splice(index, 1);
    setConfigList(newConfigList);
  };

  const onConfigNameChange = (e) => {
    let newConfigList = [...configList];
    if (type === "applyCluster" && data?.groupName === defaultGroupNames) {
      setDefaultGroupNames(e.target.value?.trim());
    }
    newConfigList[index].groupName = e.target.value?.trim();
    setConfigList(newConfigList);
  };

  const onRadioChange = (e) => {
    let value = e?.target?.value;
    let newConfigList = [...configList];
    newConfigList.forEach((item) => {
      item.fileConfig.forEach((file) => {
        file.value = file?.editor?.getValue() || file?.value;
      });
      item.runningConfig[0].value = item.runningConfig[0].value;
      item.systemConfig[0].value = item.systemConfig[0].value;
    });
    setActiveRadioKey(value);
    setConfigList(newConfigList);
  };

  const onTableChange = (val) => {
    let newConfigList = [...configList];
    newConfigList[index].ipList = val;
    setConfigList(newConfigList);
  };

  const renderLinkAddress = () => {
    if (type === "applyCluster") {
      // defaultGroupNames 为当前配置组名称或无 defaultGroupNames 的情况，才展示集群连接地址
      if (defaultGroupNames === data?.groupName || !defaultGroupNames) {
        return (
          <div className="cluster-link-address">
            <Checkbox
              defaultChecked={defaultGroupNames === data?.groupName}
              onChange={(e) => {
                let checked = e.target.checked;
                checked ? setDefaultGroupNames(data?.groupName) : setDefaultGroupNames(undefined);
              }}
            ></Checkbox>
            <span>集群连接地址</span>
          </div>
        );
      }
    }
    return null;
  };

  return (
    <div className={`config-group-box ${className ? className : ""}`}>
      <div className="config-group">
        <Form.Item
          name={`config-group-name-${groupKey}`}
          label="配置组名称"
          initialValue={data ? data?.groupName : undefined}
          rules={[
            {
              validator: (rule: any, value: string) => {
                const reg = /^[a-zA-Z0-9\u4e00-\u9fa5_-]{4,32}$/g;
                if (!reg.test(value) || !value) {
                  return Promise.reject("请填写配置组名称，支持中英文、数字、-、_、4-32位字符");
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          {type === "plugin-edit" || (type === "installPlugin" && plugType === "engine") ? (
            <div>{data?.groupName || "-"}</div>
          ) : (
            <Input className="name-input" placeholder="请输入配置名称" onBlur={onConfigNameChange}></Input>
          )}
        </Form.Item>
        {renderLinkAddress()}
        <Form.Item
          className="edit-config-radio"
          name={`config-group-type-${groupKey}`}
          label="配置类型"
          initialValue={1}
          extra={
            <EditConfigTab
              data={data ? data : JSON.parse(JSON.stringify(newGroupData))}
              index={index || 0}
              configList={configList?.length ? configList : [JSON.parse(JSON.stringify(newGroupData))]}
              setConfigList={setConfigList}
              activeRadioKey={activeRadioKey}
              type={type}
            />
          }
        >
          <Radio.Group options={configTypeOptions} onChange={onRadioChange} value={activeRadioKey} optionType="button" />
        </Form.Item>
        <div className="config-edit-btn">
          {(type === "installPlugin" && plugType === "engine") || type === "plugin-edit" ? null : (
            <>
              {configList?.length <= 1 ? null : (
                <span className="delete-row" onClick={onDeleteConfig}>
                  -
                </span>
              )}
              {addSoftware ? (
                <Tooltip
                  title="如需多个配置组（如集群新建时需要将不同的节点角色区分不同的配置），请点击“+”进行配置组添加"
                  getPopupContainer={(node) => node.parentElement}
                  overlayClassName="software-config-tooltip"
                  placement="topRight"
                >
                  <span className="add-row" onClick={onAddConfig}>
                    +
                  </span>
                </Tooltip>
              ) : (
                <span className="add-row" onClick={onAddConfig}>
                  +
                </span>
              )}
            </>
          )}
        </div>
      </div>
      {(type === "applyCluster" || type === "addGateway") && (
        <ApplyConfigGroupList
          data={data}
          machineList={machineList}
          index={index || 0}
          type={type}
          configList={configList?.length ? configList : [JSON.parse(JSON.stringify(newGroupData))]}
          setConfigList={setConfigList}
        />
      )}
      {type === "installPlugin" && (
        <InstallPluginTable
          data={data?.hosts}
          index={index || 0}
          plugType={plugType}
          configList={configList?.length ? configList : [JSON.parse(JSON.stringify(newGroupData))]}
          setConfigList={setConfigList}
        />
      )}
    </div>
  );
};
