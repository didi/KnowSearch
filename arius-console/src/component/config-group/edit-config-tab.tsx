import React, { useState, useRef, useEffect } from "react";
import { Tabs, Input } from "antd";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";
import { SystemRunningTable } from "./system-running-table";
import "./index.less";

const { TabPane } = Tabs;

export const EditConfigTab = (props) => {
  const { type, data, index, configList, setConfigList, activeRadioKey } = props;
  const [activeTabKey, setActiveTabKey] = useState(data?.fileConfig?.[0]?.key);
  const [items, setItems] = useState(data?.fileConfig);

  const newTabIndex = useRef(1);

  useEffect(() => {
    let currentTab = activeRadioKey === 1 ? data?.fileConfig : activeRadioKey === 2 ? data?.systemConfig : data?.runningConfig;
    setActiveTabKey(currentTab?.[0]?.key);
    setItems(currentTab || items);
  }, [activeRadioKey]);

  const onTabChange = (newActiveKey: string) => {
    setActiveTabKey(newActiveKey);
  };

  const addFile = () => {
    let currentTab = activeRadioKey === 1 ? "fileConfig" : activeRadioKey === 2 ? "systemConfig" : "runningConfig";
    const newActiveKey = `newFile-${++newTabIndex.current}`;
    const newPanes = [...items];
    newPanes.push({ label: `New File${newTabIndex.current}`, value: "", key: newActiveKey });
    const newConfigList = [...configList];
    newConfigList[index][activeRadioKey] = newPanes;
    newConfigList[index][currentTab] = newConfigList[index][activeRadioKey];
    setConfigList(newConfigList);
    setItems(newPanes);
    setActiveTabKey(newActiveKey);
  };

  const removeFile = (targetKey: string) => {
    let currentTab = activeRadioKey === 1 ? "fileConfig" : activeRadioKey === 2 ? "systemConfig" : "runningConfig";
    let newActiveKey = activeTabKey;
    let lastIndex = -1;
    items.forEach((item, i) => {
      if (item.key === targetKey) {
        lastIndex = i - 1;
      }
    });
    const newPanes = items.filter((item) => item.key !== targetKey);
    if (newPanes.length && newActiveKey === targetKey) {
      if (lastIndex >= 0) {
        newActiveKey = newPanes[lastIndex].key;
      } else {
        newActiveKey = newPanes[0].key;
      }
    }
    const newConfigList = [...configList];
    newConfigList[index][activeRadioKey] = newPanes;
    newConfigList[index][currentTab] = newConfigList[index][activeRadioKey];
    setConfigList(newConfigList);
    setItems(newPanes);
    setActiveTabKey(newActiveKey);
  };

  const onEdit = (targetKey: string, action: "add" | "remove") => {
    if (action === "add") {
      addFile();
    } else {
      removeFile(targetKey);
    }
  };

  const onFileNameChange = (val, activeTabIndex) => {
    const newConfigList = [...configList];
    let currentTab = activeRadioKey === 1 ? "fileConfig" : activeRadioKey === 2 ? "systemConfig" : "runningConfig";
    if (val?.trim()?.length) newConfigList[index][currentTab][activeTabIndex].label = val?.trim();
    newConfigList[index][currentTab][activeTabIndex].isEdit = false;
    setConfigList(newConfigList);
  };

  const onTabClick = (key) => {
    let currentTab = activeRadioKey === 1 ? "fileConfig" : activeRadioKey === 2 ? "systemConfig" : "runningConfig";
    const activeTabIndex = items.findIndex((item) => item.key === key);

    if (key === activeTabKey) {
      const newConfigList = [...configList];
      newConfigList[index][currentTab][activeTabIndex].isEdit = true;
      setConfigList(newConfigList);
    }
  };

  return (
    <Tabs
      type={"editable-card"}
      className={`file-tab ${activeRadioKey !== 1 ? "noneed-tab" : ""}`}
      size="small"
      onChange={onTabChange}
      onTabClick={onTabClick}
      activeKey={activeTabKey}
      onEdit={onEdit}
      addIcon={
        <div className="add-file">
          <span className="add">+ </span> 添加
        </div>
      }
    >
      {activeRadioKey === 1 ? (
        (items || []).map((item, index) => {
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
              closable={items.length > 1}
            >
              <div className="config-group-editor-container">
                <JsonEditorWrapper
                  data={item.value || item?.editor?.getValue() || ""}
                  title={""}
                  loading={false}
                  isNeedHeader={false}
                  readOnly={type === "plugin-edit" && item.label === "systemConfig" ? true : undefined}
                  mode="ace/mode/yaml"
                  setEditorInstance={(editor) => {
                    item.editor = editor;
                    let update = (content) => {
                      let shouldShow = !editor.session.getValue().length;
                      let node = editor.renderer.emptyMessageNode;
                      if (!shouldShow && node) {
                        editor.renderer.scroller.removeChild(editor.renderer.emptyMessageNode);
                        editor.renderer.emptyMessageNode = null;
                      } else if (shouldShow && !node) {
                        node = editor.renderer.emptyMessageNode = document.createElement("div");
                        node.textContent = content;
                        node.className = "system-config-placeholder";
                        editor.renderer.scroller.appendChild(node);
                      }
                    };
                    if (type === "applyCluster" && activeRadioKey === 1) {
                      editor.on("input", () => update("请完善以下两个默认字段：cluster.name、http.port"));
                      setTimeout(() => update("请完善以下两个默认字段：cluster.name、http.port"), 100);
                    }
                  }}
                  onEditorChange={(val) => {
                    item.value = val;
                  }}
                  jsonClassName={"config-group-editor"}
                />
              </div>
            </TabPane>
          );
        })
      ) : (
        <SystemRunningTable
          data={data}
          index={index}
          activeRadioKey={activeRadioKey}
          configList={configList}
          setConfigList={setConfigList}
        />
      )}
    </Tabs>
  );
};
