import * as React from "react";
import Url from "lib/url-parser";
import { Button, PageHeader, message } from "antd";
import { getIndexBaseInfo, getSetting, updateIndexSettingInfo } from "api/cluster-index-api";
import { CancelActionModal } from "container/custom-component";
import { InfoItem } from "component/info-item";
import { getFormatJsonStr } from "lib/utils";
import "../create/index.less";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";

export class JsonSetting extends React.Component<any> {
  private isModifyPage: boolean = true;
  private isDetailPage: boolean = false;
  private indexId: number = null;
  private history: string = null;
  static defaultProps: { isShowPlaceholder: boolean } = { isShowPlaceholder: true };

  constructor(props: any) {
    super(props);
    const url = Url();
    this.indexId = Number(url.search.id);
    this.history = unescape(url.search.history);
    this.isModifyPage = window.location.pathname.includes("modify/setting");
    this.isDetailPage = window.location.pathname.includes("/detail");
  }

  public state = {
    pageLoading: true,
    btnLoading: false,
    disabled: false,
    indexBaseInfo: {} as any,
    activeInstance: null,
    setting: "",
  };

  public componentDidMount() {
    getSetting(this.indexId)
      .then((res) => {
        this.setState({ setting: getFormatJsonStr(res?.settings) });
      })
      .finally(() => {
        this.setState({ pageLoading: false });
      });
    if (this.isModifyPage) {
      getIndexBaseInfo(this.indexId).then((info = {}) => {
        this.setState({
          indexBaseInfo: info,
        });
      });
    }
  }

  public componentWillUnmount() {
    this.setState = () => {
      return;
    };
  }

  public onSave = () => {
    try {
      const editor = this.state.activeInstance;
      const value = editor ? editor.getValue() : "";
      let jsonValue = {};
      if (value) {
        jsonValue = JSON.parse(value || "null");
      }
      const params = {
        logicId: this.indexId,
        params: value ? JSON.parse(value) : {},
      };
      this.updataBtnLoading(true);
      updateIndexSettingInfo(params)
        .then(() => {
          message.success("编辑成功");
          this.clearStore("/es/index-tpl/management/modify/setting");
          setTimeout(() => {
            this.props?.history?.push(this.history.replace("/es", ""));
          }, 100);
        })
        .finally(() => {
          this.updataBtnLoading(false);
        });
    } catch {
      message.error("JSON格式有误");
    }
  };

  public updataBtnLoading = (b: boolean) => {
    this.setState({
      btnLoading: b,
    });
  };

  // 关闭页面跳转
  public clearStore = (str) => {
    this.props?.setRemovePaths([str]);
  };

  setValid = (valid: boolean) => {
    this.setState({
      disabled: !valid,
    });
  };

  public render() {
    const { pageLoading, setting } = this.state;

    return (
      <div id="settingName">
        {this.isModifyPage ? (
          <PageHeader className="detail-header" backIcon={false}>
            {[
              {
                label: "模板名称",
                key: "name",
              },
              {
                label: "所属集群",
                key: "cluster",
              },
            ].map((row, index) => (
              <InfoItem key={index} label={row.label} value={`${this.state.indexBaseInfo?.[row.key] || "-"}`} width={250} />
            ))}
          </PageHeader>
        ) : null}

        <div className={this.isModifyPage ? "content-wrapper" : ""}>
          <JsonEditorWrapper
            wrapperClassName={this.isDetailPage ? "detail" : ""}
            data={setting}
            isNeedAutoIndent={!this.isDetailPage}
            title={"Setting编辑器"}
            loading={pageLoading}
            docType="setting"
            isNeedHeader={!this.isDetailPage}
            setEditorInstance={(editor) => {
              this.setState({
                activeInstance: editor,
              });
            }}
            isShowMappingTip={false}
            setValid={this.setValid}
            jsonClassName={this.isDetailPage ? "tpl-detail-mapping" : "tpl-edit-mapping"}
            readOnly={this.isDetailPage}
          />
          {this.isModifyPage && (
            <div className="op-btns-group">
              <Button loading={this.state.btnLoading} disabled={this.state.disabled} type="primary" onClick={this.onSave}>
                确定
              </Button>
              <CancelActionModal routeHref={this.history} history={this.props.history} cb={this.clearStore} />
            </div>
          )}
        </div>
      </div>
    );
  }
}
