import React, { useRef } from "react";
import { ACEJsonEditor } from "@knowdesign/kbn-sense/lib/packages/kbn-ace/src/ace/json_editor";
import { Spin, Tooltip } from "antd";
import { CopyOutlined } from "@ant-design/icons";
import { copyString } from "lib/utils";
import { esDocLink, formatRequestBodyDoc, mappingExample } from "./config";
import "./index.less";

interface IProps {
  wrapperClassName?: string;
  readOnly?: boolean;
  data: any;
  isShowTipLink?: boolean;
  isShowMappingTip?: boolean;
  isNeedAutoIndent?: boolean;
  isNeedHeader?: boolean;
  title: string;
  loading: boolean;
  setEditorInstance?: any;
  setValid?: any;
  jsonClassName?: string;
  docType?: string;
  docUrl?: string;
  exampleElement?: React.ReactNode;
}

export const mappingExampleElement = (title = "ES索引mapping样例：") => {
  return (
    <>
      <div>
        {title}
        <CopyOutlined onClick={() => copyString(mappingExample)} />
      </div>
      <pre>{mappingExample}</pre>
    </>
  );
};

export const JsonEditorWrapper = (props: IProps) => {
  const {
    wrapperClassName,
    readOnly,
    data,
    docType,
    title,
    loading,
    setEditorInstance,
    setValid,
    jsonClassName,
    exampleElement,
    docUrl,
    isShowTipLink = true,
    isShowMappingTip = true,
    isNeedAutoIndent = false,
    isNeedHeader = true,
  } = props;

  const currentEditor = useRef() as any;

  const setAceEditorInstance = (editor) => {
    currentEditor.current = editor;
    setEditorInstance && setEditorInstance(editor);
  };

  return (
    <Spin spinning={loading}>
      <div className={`json-editor-wrapper ${wrapperClassName || ""}`}>
        {isNeedHeader ? (
          <div className="json-content-title">
            <div className="title">{title}</div>
            <div className="tips-container">
              {isShowTipLink ? (
                <div className="tip link">
                  <a href={docUrl || esDocLink[docType]} rel="noreferrer" target="_blank">
                    官方指导
                  </a>
                </div>
              ) : null}
              {isShowMappingTip ? (
                <div className="tip">
                  <Tooltip overlayClassName="tip-example" title={exampleElement || mappingExampleElement()} placement="bottom">
                    查看填写示例
                  </Tooltip>
                </div>
              ) : null}
              {isNeedAutoIndent ? (
                <div className="tip">
                  <span
                    onClick={async (event) => {
                      event.preventDefault();
                      if (currentEditor.current) {
                        const value = formatRequestBodyDoc(currentEditor.current.getValue());
                        if (value.changed) {
                          currentEditor.current.setValue(value.data, -1);
                        }
                      }
                    }}
                  >
                    自动缩进
                  </span>
                </div>
              ) : null}
            </div>
          </div>
        ) : null}
        {!loading && (
          <ACEJsonEditor
            setEditorInstance={setAceEditorInstance}
            setValid={setValid}
            className={jsonClassName}
            readOnly={readOnly}
            data={data}
          />
        )}
      </div>
    </Spin>
  );
};
