import { Drawer, Tag } from "antd";
import React from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { getFormatJsonStr } from "lib/utils";
import "./mapping-diff.less";

import CodeMirror from "codemirror"; //引入codeMirror
import "codemirror/addon/merge/merge.css"; //引入codeMirror样式
import "codemirror/addon/merge/merge.css";
import "codemirror/addon/merge/merge.js";
import "codemirror/mode/javascript/javascript";
import "codemirror/mode/xml/xml";
import "codemirror/mode/python/python";
import "codemirror/mode/markdown/markdown";
import "codemirror/addon/fold/foldgutter.css";
import "codemirror/addon/fold/foldcode";
import "codemirror/addon/fold/brace-fold"; //折叠js
import "codemirror/addon/fold/xml-fold"; //折叠xml和html
import "codemirror/addon/fold/markdown-fold"; //折叠md
import "codemirror/addon/fold/comment-fold"; //折叠注释，但是测试一下只能折叠html的注释；
import "codemirror/addon/selection/active-line";
import DiffMatchPatch from "diff-match-patch";
(window as any).diff_match_patch = DiffMatchPatch;
(window as any).DIFF_DELETE = -1;
(window as any).DIFF_INSERT = 1;
(window as any).DIFF_EQUAL = 0;

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
});
class MappingSettingDiff extends React.Component<any> {
  public constructor(props) {
    super(props);
  }

  public componentDidMount() {
    this.initUI(this.props.params?.data);
  }

  public componentWillReceiveProps(nextProps) {
    this.initUI(nextProps.params?.data);
  }

  public initUI(data) {
    let operate = this.props.params?.operate;
    const target = document.querySelector(".react-diff-code-view"); // 获取dom元素
    const oldData = data?.source;
    const newData = data?.target;
    // target.innerHTML = ""; // 每次dom元素的内容清空
    CodeMirror.MergeView(
      target,
      Object.assign(
        {},
        {
          readOnly: true, // 只读
          lineNumbers: true, // 显示行号
          // theme: 'eclipse', // 设置主题
          value: operate === "配置文件" ? newData || "" : getFormatJsonStr(newData), // 右边的内容（新内容）
          orig: operate === "配置文件" ? oldData || "" : getFormatJsonStr(oldData), // 左边的内容（旧内容）
          mode: "javascript", // 代码模式为js模式，这里还可以是xml，python，java，等等，会根据不同代码模式实现代码高亮
          highlightDifferences: "highlight", // 有差异的地方是否高亮
          connect: null,
          revertButtons: false, // revert按钮设置为true可以回滚
          // styleActiveLine: true, // 光标所在的位置代码高亮
          lineWrap: true, // 文字过长时，是换行(wrap)还是滚动(scroll),默认是滚动
          smartIndent: true, // 智能缩进
          matchBrackets: true, // 括号匹配
          foldGutter: true, // 代码折叠
          gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
        }
      )
    );
    setTimeout(() => {
      const scrolllocks = document.querySelectorAll(".CodeMirror-merge-scrolllock");
      const gaps = document.querySelectorAll(".CodeMirror-merge-gap");
      if (scrolllocks && scrolllocks.length) {
        scrolllocks.forEach((item: any) => {
          item.click();
        });
      }
      if (gaps && gaps.length) {
        gaps.forEach((item: any) => {
          item?.setAttribute("style", `visibility: hidden `);
        });
      }
    }, 300);
  }

  public render() {
    const { dispatch, params } = this.props;
    const operate = params?.operate;
    return (
      <Drawer
        title={`${operate}对比`}
        visible={true}
        onClose={() => dispatch(actions.setDrawerId(""))}
        width={1080}
        maskClosable={true}
        bodyStyle={{ padding: "0px" }}
      >
        <div className="mapping-diff">
          <div className="mapping-diff-mapping">
            <div className="mapping-diff-mapping-header">
              <div className="mapping-diff-mapping-header-before">
                {`${operate}${operate.includes("配置") ? "" : "编辑器"}`}
                <Tag style={{ color: "#5B6675", marginLeft: 10 }}>{operate.includes("配置") ? "变更前" : "编辑前"}</Tag>
              </div>
              <div className="mapping-diff-mapping-header-after">
                {`${operate}${operate.includes("配置") ? "" : "编辑器"}`}
                <Tag color="green" style={{ marginLeft: 10 }}>
                  {operate.includes("配置") ? "变更后" : "编辑后"}
                </Tag>
              </div>
            </div>
            <div className="react-diff-code-view"></div>
          </div>
        </div>
      </Drawer>
    );
  }
}
export default connect(mapStateToProps)(MappingSettingDiff);
