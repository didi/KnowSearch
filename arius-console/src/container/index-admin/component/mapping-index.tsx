import * as React from "react";
import Url from "lib/url-parser";
import { message } from "antd";
import { connect } from "react-redux";
import { getMapping, updateMapping } from "api/index-admin";
import { getFormatJsonStr } from "lib/utils";
import "./index.less";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});
const connects: Function = connect;
@connects(mapStateToProps)
export class SetMapping extends React.Component<any> {
  private indexName: string = null;
  private isDetailPage: boolean = false;
  private isModifyPage: boolean = false;
  static defaultProps: { isShowPlaceholder: boolean } = { isShowPlaceholder: true };

  public state = {
    loading: false,
    activeInstance: null,
  };

  constructor(props: any) {
    super(props);
    this.indexName = Url().search.index || this.props.params.index;
    this.isDetailPage = window.location.pathname.includes("/detail");
    this.isModifyPage = !this.isDetailPage && this.indexName ? true : false;
  }

  public componentDidMount() {
    this.props.childEvevnt(this);
    if (this.isModifyPage || this.isDetailPage) {
      this.setState({
        loading: true,
      });
      getMapping(this.props.params.cluster, this.indexName)
        .then((res) => {
          this.props.updateState({
            mapping: getFormatJsonStr(JSON.parse(res?.mappings)),
          });
        })
        .finally(() => {
          this.setState({
            loading: false,
          });
        });
    }
  }

  public componentWillUnmount() {
    if (this.isModifyPage || this.isDetailPage) {
      this.props.updateState({
        mapping: "",
      });
    }
  }

  public handlePre = () => {
    const editor = this.state.activeInstance;
    const value = editor ? editor.getValue() : "";
    this.props.updateState({
      current: 0,
      mapping: value,
    });
  };

  public handleSubmit = () => {
    try {
      const editor = this.state.activeInstance;
      const value = editor ? editor.getValue() : "";
      let jsonValue = {};
      if (value) {
        jsonValue = JSON.parse(value || "null");
      }
      this.props.updateState({
        current: 2,
        mapping: value,
      });
    } catch {
      message.error("JSON格式有误");
    }
  };

  public handleSave = (cb) => {
    try {
      const editor = this.state.activeInstance;
      const value = editor ? editor.getValue() : "";
      let jsonValue = {};
      if (value) {
        jsonValue = JSON.parse(value || "null");
      }
      const params = {
        cluster: this.props.params.cluster,
        index: this.indexName,
        mapping: JSON.stringify(jsonValue),
      };
      this.props.updateState({
        btnLoading: true,
      });
      updateMapping(params)
        .then(() => {
          message.success("编辑成功");
          cb && cb();
        })
        .finally(() => {
          this.props.updateState({
            btnLoading: false,
          });
        });
    } catch {
      message.error("JSON格式有误");
    }
  };

  public render() {
    const { data: value, setValid } = this.props;
    const { loading } = this.state;

    return (
      <>
        <JsonEditorWrapper
          data={value}
          title={"Mapping编辑器"}
          loading={loading}
          docType="mapping"
          isNeedAutoIndent={!this.isDetailPage}
          setEditorInstance={(editor) => {
            this.setState({
              activeInstance: editor,
            });
          }}
          setValid={setValid}
          jsonClassName={this.isModifyPage ? "edit-mapping info" : "edit-mapping"}
          readOnly={this.isDetailPage}
        />
      </>
    );
  }
}
