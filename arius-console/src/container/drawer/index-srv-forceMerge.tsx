import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { message, Tooltip } from "antd";
import { regNonnegativeInteger } from "constants/reg";
import { executeForceMerge } from "api/index-admin";
import { XNotification } from "component/x-notification";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const IndexSrvForceMerge = connect(mapStateToProps)((props: { dispatch: any; cb: Function; params: any }) => {
  const [onlyExpungeDeletes, setOnlyExpungeDeletes] = React.useState(false);

  const xFormModalConfig = {
    formMap: [
      {
        key: "onlyExpungeDeletes",
        label: (
          <div className="cluster-label">
            only_expunge_deletes
            <Tooltip title="是否只合并包含删除文档的segment">
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),

        type: FormItemType.select,
        attrs: {
          placeholder: "请选择",
          onChange: (value) => {
            setOnlyExpungeDeletes(value);
          },
        },
        rules: [
          {
            required: true,
            message: "请选择only_expunge_deletes",
          },
        ],
        options: [
          {
            label: "true",
            value: true,
          },
          {
            label: "false",
            value: false,
          },
        ],
      },
      {
        key: "maxNumSegments",
        label: (
          <div className="cluster-label">
            max_num_segments
            <Tooltip title="最大segment数">
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.inputNumber,
        invisible: onlyExpungeDeletes,
        attrs: {
          placeholder: "请输入max_num_segments",
          style: {
            width: "100%",
          },
        },
        rules: [
          {
            required: true,
            message: `请输入max_num_segments（正整数）`,
            validator: (rule: any, value: any) => {
              if (new RegExp(regNonnegativeInteger).test(value)) return Promise.resolve();
              return Promise.reject();
            },
          },
        ],
      },
      {
        key: "indexs",
        label: "操作对象",
        type: FormItemType.custom,
        customFormItem: (
          <div className="btn-labels-box">
            {props.params.length ? props.params.map((item) => <div className="btn-labels">{item.index}</div>) : "-"}
          </div>
        ),
      },
    ] as IFormItem[],
    type: "drawer",
    visible: true,
    title: (
      <>
        执行ForceMerge
        <a
          href="https://www.elastic.co/guide/en/elasticsearch/reference/7.10/indices-forcemerge.html"
          rel="noreferrer"
          target="_blank"
          className="link-button"
        >
          官方指导
        </a>
      </>
    ),
    formData: { onlyExpungeDeletes: false, maxNumSegments: 5 },
    isWaitting: true,
    width: 500,
    needSuccessMessage: false,
    onCancel: () => {
      props.dispatch(actions.setDrawerId(""));
    },
    onSubmit: (result: any) => {
      const params = {
        indices: props.params.map((item) => ({
          cluster: item.cluster,
          index: item.index,
        })),
        ...result,
      };
      return executeForceMerge(params).then(() => {
        message.success(`操作提交成功，任务异步执行`);
        props.dispatch(actions.setDrawerId(""));
        props.cb && props.cb(); // 重新获取数据列表
      });
      // .catch((err) => {
      //   XNotification({ type: "error", message: "错误", description: err.tips });
      // });
    },
  };

  return (
    <>
      <XFormWrapper {...xFormModalConfig} />
    </>
  );
});
