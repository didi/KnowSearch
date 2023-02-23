import React, { useState, useEffect } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { getPluginStatus, updatePluginConfig } from "api/plug-api";
import { EditConfigGroup } from "component/config-group";
import { uuid } from "lib/utils";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import Url from "lib/url-parser";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const EditPlugin = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const { params, dispatch, cb } = props;
  const [configList, setConfigList] = useState([]);
  const [originConfig, setOriginConfig] = useState({} as any);

  useEffect(() => {
    getConfigList();
  }, []);

  const getConfigList = async () => {
    let originConfig: any = { fileConfig: {} };
    let res = await getPluginStatus(params.id);
    let list = (res || []).map((item, index) => {
      let file = JSON.parse(item.fileConfig) || {};
      originConfig.fileConfig = file;
      originConfig.systemConfig = item.systemConfig;
      originConfig.runningConfig = item.runningConfig;
      let keys = Object.keys(file);
      let fileConfig = keys.map((fileItem, index) => {
        return {
          label: fileItem,
          key: `newFile-${index}`,
          isEdit: false,
          value: file[fileItem],
        };
      });
      return {
        groupName: item.groupName,
        fileConfig,
        systemConfig: [{ value: item.systemConfig, key: `systemConfig-${index}`, label: "systemConfig" }],
        runningConfig: [{ value: item.runningConfig, key: `runningConfig-${index}`, label: "runningConfig" }],
        hosts: item?.hosts,
        installDirectoryConfig: item?.installDirectoryConfig,
        processNumConfig: item?.processNumConfig,
        key: uuid(),
      };
    });
    setConfigList(list);
    setOriginConfig(originConfig);
  };

  const getGroupConfigList = () => {
    let newConfigList = [];
    let newConfig: any = { fileConfig: {} };
    configList.forEach((item) => {
      let fileConfig = {};
      (item.fileConfig || []).forEach((file) => {
        fileConfig[file.label] = file.editor.getValue() || file.value;
        newConfig.fileConfig[file.label] = file.editor.getValue() || file.value;
      });
      newConfig.systemConfig = item.systemConfig?.[0]?.value || "";
      newConfig.runningConfig = item.runningConfig?.[0]?.value || "";
      let config = {
        groupName: item.groupName,
        fileConfig: JSON.stringify(fileConfig),
        systemConfig: item.systemConfig?.[0]?.value || "",
        runningConfig: item.runningConfig?.[0]?.value || "",
        hosts: item?.hosts,
        installDirectoryConfig: item?.installDirectoryConfig,
        processNumConfig: item?.processNumConfig,
      };
      newConfigList.push(config);
    });
    return { configList: newConfigList, newConfig };
  };

  const getChangeNames = (newConfig) => {
    let changeNames = [];
    let newFileKeys = Object.keys(newConfig.fileConfig);
    let oldFileKeys = Object.keys(originConfig.fileConfig);
    newFileKeys.forEach((item) => {
      // 添加了新文件, 或新旧文件内容不一致
      if (!oldFileKeys.includes(item) || newConfig.fileConfig[item] !== originConfig.fileConfig[item]) {
        changeNames.push(item);
      }
      // 删除了文件
      oldFileKeys.forEach((ele) => {
        if (!newFileKeys.includes(ele)) {
          changeNames.push(item);
        }
      });
    });
    if (newConfig.systemConfig !== originConfig.systemConfig) {
      changeNames.push("系统配置");
    }
    if (newConfig.runningConfig !== originConfig.runningConfig) {
      changeNames.push("运行时配置");
    }
    return changeNames;
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "name",
        label: "插件名称",
        type: FormItemType.custom,
        customFormItem: <span>{params.name}</span>,
        isCustomStyle: true,
        CustomStyle: {
          marginTop: 24,
        },
      },
      {
        key: "config",
        label: "",
        type: FormItemType.custom,
        customFormItem: configList.length ? (
          <>
            {configList.map((item, index) => {
              return (
                <EditConfigGroup
                  key={item.key}
                  type="plugin-edit"
                  groupKey={item.key}
                  data={item}
                  index={index}
                  configList={configList}
                  setConfigList={setConfigList}
                />
              );
            })}
          </>
        ) : (
          <EditConfigGroup type="plugin-edit" setConfigList={setConfigList} configList={configList} />
        ),
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: "配置变更",
    needBtnLoading: true,
    width: 668,
    onCancel: () => {
      dispatch(actions.setDrawerId(""));
    },
    onSubmit: async () => {
      let groupConfig = getGroupConfigList();
      let changeNames = getChangeNames(groupConfig.newConfig);
      let data = {
        componentId: params.componentId,
        pluginType: params.pluginType,
        dependComponentId: +Url().search.componentId,
        groupConfigList: groupConfig.configList,
        changeNames,
      };
      let expandData = { expandData: JSON.stringify(data) };
      let res = await updatePluginConfig(expandData);
      showSubmitTaskSuccessModal(res, props.params?.history);
      dispatch(actions.setDrawerId(""));
      cb && cb();
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
