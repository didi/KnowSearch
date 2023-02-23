import React, { useState, useEffect, useRef, createRef } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { FormItemType, IFormItem } from "component/x-form";
import { connect } from "react-redux";
import * as actions from "actions";
import { updatePhyClusterConfig } from "api/cluster-api";
import { editGatewayConfig } from "api/gateway-manage";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";
import { Tabs, Input } from "antd";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import _ from "lodash";
import { uuid } from "lib/utils";
import "./config-edit.less";

const { TabPane } = Tabs;

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const EditConfigGroup = connect(mapStateToProps)((props: { dispatch: any; params: any; cb: any }) => {
  const {
    params: { type, record },
    dispatch,
    cb,
  } = props;
  const [configData, setConfigData] = useState<any>({});
  const [originConfig, setOriginConfig] = useState({} as any);

  const $formRef: any = createRef();

  useEffect(() => {
    getConfigData();
  }, []);

  const getConfigData = () => {
    let file = {};
    let originConfig: any = { fileConfig: {} };
    try {
      file = JSON.parse(record.fileConfig);
      originConfig.fileConfig = file;
    } catch {}
    let keys = Object.keys(file);
    let fileConfig = keys.map((fileItem, index) => {
      return {
        label: fileItem,
        key: `newFile-${index}`,
        isEdit: false,
        value: file[fileItem],
      };
    });
    originConfig.systemConfig = record.systemConfig;
    originConfig.runningConfig = record.runningConfig;
    let config = {
      groupName: record.groupName,
      fileConfig,
      systemConfig: { value: record.systemConfig, label: "systemConfig" },
      runningConfig: { value: record.runningConfig, label: "runningConfig" },
      key: uuid(),
    };
    setConfigData(config);
    setOriginConfig(originConfig);
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "groupName",
        label: "配置名称",
        type: FormItemType.text,
        customFormItem: (
          <>
            <span>{configData?.groupName || ""}</span>
          </>
        ),
        isCustomStyle: true,
        CustomStyle: {
          marginTop: 24,
        },
      },
      {
        key: "config",
        label: "配置类型",
        type: FormItemType.custom,
        customFormItem: (
          <>
            <CustomFormItem readOnly={type !== "edit"} configData={configData} setConfigData={setConfigData} />
          </>
        ),
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: type === "edit" ? "编辑" : "查看",
    nofooter: type !== "edit",
    needBtnLoading: true,
    width: 668,
    onCancel: () => {
      dispatch(actions.setDrawerId(""));
    },
    onSubmit: async (result: any) => {
      let fileConfig = {};
      let systemConfig = configData.systemConfig.editor?.getValue() || configData.systemConfig?.value || "";
      let runningConfig = configData.runningConfig.editor?.getValue() || configData.runningConfig?.value || "";
      let changeNames = [];
      (configData.fileConfig || []).forEach((file) => {
        fileConfig[file.label] = file.editor.getValue() || file.value;
      });
      let newFileKeys = Object.keys(fileConfig);
      let oldFileKeys = Object.keys(originConfig.fileConfig);
      newFileKeys.forEach((item) => {
        // 添加了新文件, 或新旧文件内容不一致
        if (!oldFileKeys.includes(item) || fileConfig[item] !== originConfig.fileConfig[item]) {
          changeNames.push(item);
        }
        // 删除了文件
        oldFileKeys.forEach((ele) => {
          if (!newFileKeys.includes(ele)) {
            changeNames.push(item);
          }
        });
      });

      if (systemConfig !== originConfig.systemConfig) {
        changeNames.push("系统配置");
      }
      if (runningConfig !== originConfig.runningConfig) {
        changeNames.push("运行时配置");
      }
      let params = {
        componentId: record.componentId,
        changeNames,
        groupConfigList: [
          {
            id: record.id,
            groupName: record.groupName,
            systemConfig,
            runningConfig,
            fileConfig: fileConfig,
            installDirectoryConfig: record.installDirectoryConfig,
            processNumConfig: record.processNumConfig,
            hosts: record.hosts,
            version: record.version,
          },
        ],
      };
      let expandData = JSON.stringify(params);
      let res = record.cluster === "gateway" ? await editGatewayConfig({ expandData }) : await updatePhyClusterConfig({ expandData });
      dispatch(actions.setDrawerId(""));
      showSubmitTaskSuccessModal(res, record.history);
      cb && cb();
    },
  };

  return (
    <>
      <XFormWrapper ref={$formRef} {...xFormModalConfig} />
    </>
  );
});

const CustomFormItem = (props: any) => {
  const { configData, readOnly, setConfigData } = props;
  const [activeTabKey, setActiveTabKey] = useState(configData?.fileConfig?.[0]?.key);
  const [fileList, setFileList] = useState([]);

  const newTabIndex = useRef(1);

  useEffect(() => {
    setFileList(configData.fileConfig);
  }, [configData]);

  const addFile = () => {
    const newActiveKey = `newFile-${++newTabIndex.current}`;
    const newPanes = _.cloneDeep(fileList);
    newPanes.push({ label: `New File${newTabIndex.current}`, value: "", key: newActiveKey });
    setActiveTabKey(newActiveKey);
    setFileList(newPanes);
    let data = _.cloneDeep(configData);
    data.fileConfig = newPanes;
    setConfigData(data);
  };

  const removeFile = (targetKey: string) => {
    let newActiveKey = activeTabKey;
    let lastIndex = -1;
    fileList.forEach((item, i) => {
      if (item.key === targetKey) {
        lastIndex = i - 1;
      }
    });
    const newPanes = fileList.filter((item) => item.key !== targetKey);
    if (newPanes.length && newActiveKey === targetKey) {
      if (lastIndex >= 0) {
        newActiveKey = newPanes[lastIndex].key;
      } else {
        newActiveKey = newPanes[0].key;
      }
    }
    setActiveTabKey(newActiveKey);
    setFileList(newPanes);
    let data = _.cloneDeep(configData);
    data.fileConfig = newPanes;
    setConfigData(data);
  };

  const onEdit = (targetKey: string, action: "add" | "remove") => {
    if (action === "add") {
      addFile();
    } else {
      removeFile(targetKey);
    }
  };

  const onTabChange = (newActiveKey: string) => {
    setActiveTabKey(newActiveKey);
  };

  const onTabClick = (key) => {
    const activeTabIndex = fileList.findIndex((item) => item.key === key);
    if (key === activeTabKey) {
      const newFileList = _.cloneDeep(fileList);
      newFileList[activeTabIndex].isEdit = true;
      setFileList(newFileList);
      let data = _.cloneDeep(configData);
      data.fileConfig = newFileList;
      setConfigData(data);
    }
  };

  const onFileNameChange = (val, activeTabIndex) => {
    const newFileList = _.cloneDeep(fileList);
    if (val?.trim()?.length) newFileList[activeTabIndex].label = val?.trim();
    newFileList[activeTabIndex].isEdit = false;
    setFileList(newFileList);
    let data = _.cloneDeep(configData);
    data.fileConfig = newFileList;
    setConfigData(data);
  };

  return (
    <>
      <div className="config-type-box">
        <div className="config-vertical-line"></div>
        <span>文件配置</span>
      </div>
      <div className="edit-config-group-box">
        <div className="config-group noneed-bg-padding">
          <Tabs
            type={"editable-card"}
            className={`file-tab`}
            size="small"
            onChange={onTabChange}
            onTabClick={onTabClick}
            onEdit={onEdit}
            hideAdd={readOnly}
            activeKey={activeTabKey}
            addIcon={
              <div className="add-file">
                <span className="add">+ </span> 添加
              </div>
            }
          >
            {(fileList || []).map((item, index) => {
              return (
                <TabPane
                  tab={
                    item.isEdit ? (
                      <Input
                        className="file-name-input"
                        allowClear
                        defaultValue={item.label}
                        onBlur={(e) => onFileNameChange(e.target.value, index)}
                      />
                    ) : (
                      <span className="file-name-text">{item.label}</span>
                    )
                  }
                  key={item.key}
                  closable={fileList.length > 1}
                >
                  <div className="config-group-editor-container">
                    <JsonEditorWrapper
                      data={item.value || item?.editor?.getValue() || ""}
                      title={""}
                      loading={false}
                      isNeedHeader={false}
                      readOnly={readOnly}
                      mode="ace/mode/yaml"
                      setEditorInstance={(editor) => {
                        item.editor = editor;
                      }}
                      onEditorChange={(val) => {
                        item.value = val;
                      }}
                      jsonClassName={"config-group-editor"}
                    />
                  </div>
                </TabPane>
              );
            })}
          </Tabs>
        </div>
      </div>
      <div className="config-system-box">
        <div className="config-vertical-line"></div>
        <span>系统配置</span>
      </div>
      <div className="config-group-editor-container">
        <JsonEditorWrapper
          key={uuid()}
          data={configData?.systemConfig?.value || configData?.systemConfig?.editor?.getValue() || ""}
          title={""}
          loading={false}
          isNeedHeader={false}
          readOnly={readOnly}
          setEditorInstance={(editor) => {
            configData.systemConfig = {};
            configData.systemConfig.editor = editor;
          }}
          onEditorChange={(val) => {
            if (configData?.systemConfig) {
              configData.systemConfig.value = val;
            }
          }}
          jsonClassName={"config-group-editor"}
        />
      </div>
      <div className="config-running-box">
        <div className="config-vertical-line"></div>
        <span>运行时配置</span>
      </div>
      <div className="config-group-editor-container">
        <JsonEditorWrapper
          key={uuid()}
          data={configData?.runningConfig?.value || configData?.runningConfig?.editor?.getValue() || ""}
          title={""}
          loading={false}
          isNeedHeader={false}
          readOnly={readOnly}
          setEditorInstance={(editor) => {
            configData.runningConfig = {};
            configData.runningConfig.editor = editor;
          }}
          onEditorChange={(val) => {
            if (configData?.runningConfig) {
              configData.runningConfig.value = val;
            }
          }}
          jsonClassName={"config-group-editor"}
        />
      </div>
    </>
  );
};
