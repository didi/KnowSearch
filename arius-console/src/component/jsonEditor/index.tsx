import React from "react";
// 引入codemirror
import { UnControlled as CodeMirror } from "react-codemirror2";
import "codemirror/lib/codemirror.css";
import "codemirror/theme/material.css";
import "./index.less";
// 代码错误提示
import "codemirror/addon/lint/lint";
import "codemirror/addon/lint/json-lint";
import "codemirror/addon/lint/lint.css";
// placeholder
import "codemirror/addon/display/placeholder";

// 没有window.jsonlint错误校验会失效
import jsonlint from "jsonlint";
// @ts-ignore
window.jsonlint = jsonlint;

interface IJsonEditorProps {
  editorDidMount: (result: any) => any;
  options?: object;
  attrs?: object;
}

export const JsonEditor = (props: IJsonEditorProps) => {
  const { editorDidMount, options, attrs } = props;

  return (
    <>
      <CodeMirror
        options={{
          // mode: "application/json",
          mode: { name: "javascript", json: true },
          lineNumbers: true, // 显示行号
          smartIndent: true, // 自动缩进
          lineWrapping: true, // 自动换行
          foldGutter: true, // 代码折叠
          gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter", "CodeMirror-lint-markers"],
          lint: true, // 错误提示
          indentUnit: 4, // 缩进配置（默认为2）
          ...options,
        }}
        editorDidMount={editorDidMount}
        {...attrs}
      />
    </>
  );
};
