import * as React from "react";
import Url from "lib/url-parser";
import { message } from "antd";
import { connect } from "react-redux";
import { getSetting, updateSetting } from "api/index-admin";
import { getFormatJsonStr, formatJsonStr } from "lib/utils";
import "./index.less";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";

const mapStateToProps = (state) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});
const connects: Function = connect;
@connects(mapStateToProps)
export class SetSetting extends React.Component<any> {
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
      getSetting(this.props.params.cluster, this.indexName)
        .then((res) => {
          this.props.updateState({
            setting: getFormatJsonStr(res?.properties),
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
        setting: "",
      });
    }
  }

  public handlePre = () => {
    const editor = this.state.activeInstance;
    const value = editor ? editor.getValue() : "";
    this.props.updateState({
      current: 1,
      setting: value,
    });
  };

  public handleNext = () => {
    try {
      const editor = this.state.activeInstance;
      const value = editor ? editor.getValue() : "";
      let jsonValue = {};
      if (value) {
        jsonValue = JSON.parse(value || "null");
      }
      this.props.updateState({
        current: 3,
        setting: value,
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
        setting: formatJsonStr(value),
      };
      this.props.updateState({
        btnLoading: true,
      });
      updateSetting(params)
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
          title={"Setting编辑器"}
          loading={loading}
          isNeedAutoIndent={!this.isDetailPage}
          docType="setting"
          setEditorInstance={(editor) => {
            this.setState({
              activeInstance: editor,
            });
          }}
          setValid={setValid}
          isShowMappingTip={false}
          jsonClassName={this.isModifyPage ? "edit-mapping modify" : "edit-mapping"}
          readOnly={this.isDetailPage}
        />
      </>
    );
  }
}
