import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { executeShrink, executeSplit } from "api/index-admin";
import { formatJsonStr, copyString } from "lib/utils";
import { CopyOutlined } from "@ant-design/icons";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const IndexSrvShrinkSplit = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const { type, indexs } = props.params;
  const [activeInstance, setActiveInstance] = React.useState(null);
  const shrinkExample = `{
    "settings": {
        "index.number_of_replicas": 1,
        "index.number_of_shards": 1,
        "index.codec": "best_compression"
    },
    "aliases": {
        "my_search_indices": {}
    }
}`;
  const splitExample = `{
    "settings": {
        "index.number_of_shards": 5
    },
    "aliases": {
        "my_search_indices": {}
    }
}`;
  const documentHref =
    type === "shrink"
      ? "https://www.elastic.co/guide/en/elasticsearch/reference/7.10/indices-shrink-index.html"
      : "https://www.elastic.co/guide/en/elasticsearch/reference/7.10/indices-split-index.html";

  const exampleElement = () => {
    const exampleText = type === "shrink" ? shrinkExample : splitExample;
    return (
      <>
        <div>
          {type === "shrink" ? "执行shrink样例：" : "执行split样例："}
          <CopyOutlined onClick={() => copyString(exampleText)} />
        </div>
        <pre>{exampleText}</pre>
      </>
    );
  };

  const xFormModalConfig = {
    formMap: [
      {
        key: "tips",
        label: "",
        type: FormItemType.custom,
        formAttrs: {
          className: "warning-container",
          style: {
            margin: "0 -24px",
            padding: "2px 24px",
          },
        },
        customFormItem: (
          <>
            <span className="icon iconfont iconbiaogejieshi"></span>
            <span>需要提前禁写索引，请谨慎操作</span>
          </>
        ),
      },
      {
        key: "targetIndex",
        label: "目标索引",
        type: FormItemType.input,
        attrs: {
          placeholder: "请填写目标索引，支持小写字母、数字、-、_4-128位字符",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              value = value?.trim();
              const reg = /^[.a-z0-9_-]{1,}$/g;
              if (!value) {
                return Promise.reject("请填写目标索引");
              }
              if (!reg.test(value) || value.length > 128 || value.length < 4) {
                return Promise.reject("请正确填写目标索引，支持小写字母、数字、-、_4-128位字符");
              }
              if (value && value === indexs.index) {
                return Promise.reject("目标索引不能与原有索引重复");
              }
              return Promise.resolve();
            },
          },
        ],
        options: [{ label: "index", value: 1 }],
      },
      {
        key: "extra",
        label: "",
        type: FormItemType.custom,
        customFormItem: (
          <>
            <JsonEditorWrapper
              data={""}
              isNeedAutoIndent={true}
              title={"编辑器"}
              loading={false}
              docType="mapping"
              setEditorInstance={(editor) => {
                setActiveInstance(editor);
              }}
              docUrl={documentHref}
              exampleElement={exampleElement()}
              jsonClassName={"index-edit-json"}
            />
          </>
        ),
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: type === "shrink" ? "执行shrink" : "执行split",
    needBtnLoading: true,
    width: 500,
    needSuccessMessage: false,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onSubmit: (result: any) => {
      try {
        const value = activeInstance ? activeInstance.getValue() : "";
        if (type === "split" && (!value || value === "{}")) {
          message.error("JSON不能为空");
          return new Promise((resolve) => {
            resolve("");
          });
        }
        let jsonValue = {};
        if (value) {
          jsonValue = JSON.parse(value || "null");
        }
        const params = {
          cluster: indexs.cluster,
          index: indexs.index,
          targetIndex: result.targetIndex,
          extra: formatJsonStr(value),
        };
        const submitFn = type === "shrink" ? executeShrink : executeSplit;
        return submitFn(params).then(() => {
          message.success(`操作提交成功，任务异步执行`);
          props.dispatch(actions.setDrawerId(""));
          props.cb && props.cb(); // 重新获取数据列表
        });
      } catch {
        message.error("JSON格式有误");
        return new Promise((resolve) => {
          resolve("");
        });
      }
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
