import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message } from "antd";
import { regNonnegativeInteger } from "constants/reg";
import { executeRollover } from "api/index-admin";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const IndexSrvRollover = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const rolloverIndexs = props.params.filter((item) => !item.templateId && item.aliases?.length);

  const xFormModalConfig = {
    formMap: [
      {
        key: "max_age",
        label: "max_age",
        type: FormItemType.input,
        attrs: {
          placeholder: "请输入max_age，单位参考，天：d、小时：h",
        },
        rules: [
          {
            required: false,
            message: `请输入max_age`,
          },
        ],
      },
      {
        key: "max_docs",
        label: "max_docs",
        type: FormItemType.inputNumber,
        attrs: {
          placeholder: "请输入max_docs",
          style: {
            width: "100%",
          },
        },
        rules: [
          {
            required: false,
            message: `请输入max_docs（正整数）`,
            validator: (rule: any, value: any) => {
              if (value === undefined || value === null) return Promise.resolve();
              if (new RegExp(regNonnegativeInteger).test(value)) return Promise.resolve();
              return Promise.reject();
            },
          },
        ],
      },
      {
        key: "max_size",
        label: "max_size",
        type: FormItemType.input,
        attrs: {
          placeholder: "请输入max_size，单位参考：gb",
        },
        rules: [
          {
            required: false,
            message: `请输入max_size`,
          },
        ],
      },
      {
        key: "indexs",
        label: "操作对象",
        type: FormItemType.custom,
        customFormItem: (
          <div className="btn-labels-box">
            {rolloverIndexs.length
              ? rolloverIndexs.map((item, index) => (
                  <div key={index} className="btn-labels">
                    {item.index}
                  </div>
                ))
              : "-"}
          </div>
        ),
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: (
      <>
        执行Rollover
        <a
          href="https://www.elastic.co/guide/en/elasticsearch/reference/7.10/indices-rollover-index.html"
          rel="noreferrer"
          target="_blank"
          className="link-button"
        >
          官方指导
        </a>
      </>
    ),
    needBtnLoading: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onSubmit: (result: any) => {
      if (!rolloverIndexs.length) {
        props.dispatch(actions.setDrawerId(""));
        return;
      }
      const params = {
        indices: rolloverIndexs.map((item) => ({
          cluster: item.cluster,
          index: item.index,
        })),
        content: JSON.stringify({ conditions: result }),
      };
      return executeRollover(params).then(() => {
        message.success(`操作成功`);
        props.dispatch(actions.setDrawerId(""));
        props.cb && props.cb(); // 重新获取数据列表
      });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
