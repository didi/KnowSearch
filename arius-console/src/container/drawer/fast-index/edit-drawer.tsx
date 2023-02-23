import React, { useState } from "react";
import { Drawer, Button, Input } from "antd";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";
import "./index.less";

export default function EditDrawer(props) {
  const { visible, isEdit, onClose, dataType, content } = props;

  const [mappingEditor, setMappingEditor] = useState(null);
  const [settingEditor, setSettingEditor] = useState(null);
  const [value, setValue] = useState(content?.targetName || content.index || "");
  const [error, setError] = useState(false);

  const onOk = () => {
    const mapping = mappingEditor ? mappingEditor.getValue() : "";
    const setting = settingEditor ? settingEditor.getValue() : "";
    isEdit && props.onOk({ mapping, setting, value });
    onClose();
  };

  return (
    <Drawer
      className="fast-index-edit-darwer"
      visible={visible}
      title={
        <div className="title-container">
          <span className="icon iconfont iconarrow-left" onClick={onClose}></span>
          <span className="divider"></span>
          <span className="title">{isEdit ? "编辑" : "查看"}Mapping/Setting</span>
          <span className="title-footer">
            <Button className="cancel" onClick={onClose}>
              取消
            </Button>
            <Button className="submit" type="primary" onClick={onOk}>
              确定
            </Button>
          </span>
          <span className="divider"></span>
        </div>
      }
      width={600}
      onClose={onClose}
    >
      <div className="container">
        <div className="name-content">
          <span className={`name${dataType === 1 ? "" : " index"}`}>
            {isEdit ? "目标" : "源"}索引{dataType === 1 ? "模板" : ""}：
          </span>
          {dataType === 1 || !isEdit ? (
            <span className="content">{content.name || content.index || value || "-"}</span>
          ) : (
            <Input
              className={`${error ? "error-input" : ""}`}
              placeholder="请输入目标索引"
              value={value}
              allowClear
              onChange={(e) => {
                let value = e.target.value;
                setValue(value);
                setError(value ? false : true);
              }}
            ></Input>
          )}
          {error && <div className="error">请输入目标索引</div>}
        </div>
        <div className="json-content">
          <div className="mapping-content">
            <span className="name">Mapping信息：</span>
            <JsonEditorWrapper
              data={isEdit ? content.mapping : content.orginMapping}
              title={""}
              loading={false}
              readOnly={isEdit ? undefined : true}
              isNeedHeader={false}
              setEditorInstance={(editor) => {
                setMappingEditor(editor);
              }}
              jsonClassName={"fast-index-edit-mapping"}
            />
          </div>
          <div className="setting-content">
            <span className="name">Setting信息：</span>
            <JsonEditorWrapper
              data={isEdit ? content.setting : content.orginSetting}
              title={""}
              loading={false}
              readOnly={isEdit ? undefined : true}
              isNeedHeader={false}
              setEditorInstance={(editor) => {
                setSettingEditor(editor);
              }}
              jsonClassName={"fast-index-edit-setting"}
            />
          </div>
        </div>
      </div>
    </Drawer>
  );
}
