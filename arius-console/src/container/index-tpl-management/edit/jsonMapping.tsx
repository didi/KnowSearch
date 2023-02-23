import * as React from "react";
import { mappingFormatJsonStr } from "lib/utils";
import Url from "lib/url-parser";
import { getIndexBaseInfo, updateIndexMappingInfo, getIndexMappingInfo } from "api/cluster-index-api";
import { PageHeader, Button, message } from "antd";
import { CancelActionModal } from "container/custom-component";
import { InfoItem } from "component/info-item";
import "../create/index.less";
import { JsonEditorWrapper } from "component/jsonEditorWrapper";

export class JsonMapping extends React.Component<any> {
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
    this.isModifyPage = window.location.pathname.includes("modify/mapping");
    this.isDetailPage = window.location.pathname.includes("/detail");
  }

  public state = {
    pageLoading: true,
    btnLoading: false,
    disabled: false,
    indexBaseInfo: {} as any,
    activeInstance: null,
    mapping: "",
  };

  public componentDidMount() {
    getIndexMappingInfo(this.indexId)
      .then((res) => {
        const data = res?.typeProperties?.[0];
        this.setState({ mapping: mappingFormatJsonStr(data?.properties, data?.["dynamic_templates"]) });
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
        typeProperties: [
          {
            properties: jsonValue,
          },
        ],
      };
      this.updataBtnLoading(true);
      updateIndexMappingInfo(params)
        .then(() => {
          message.success("编辑成功");
          this.clearStore(["/es/index-tpl/management/modify/mapping"]);
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
    const { pageLoading, mapping } = this.state;

    return (
      <div id="mappingName">
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
            isNeedAutoIndent={!this.isDetailPage}
            wrapperClassName={this.isDetailPage ? "detail" : ""}
            data={mapping}
            title={"Mapping编辑器"}
            loading={pageLoading}
            docType="mapping"
            isNeedHeader={!this.isDetailPage}
            setEditorInstance={(editor) => {
              this.setState({
                activeInstance: editor,
              });
            }}
            setValid={this.setValid}
            jsonClassName={this.isDetailPage ? "tpl-detail-mapping" : "tpl-edit-mapping"}
            readOnly={this.isDetailPage}
          />
          {this.isModifyPage && (
            <div className="op-btns-group">
              <Button disabled={this.state.disabled} loading={this.state.btnLoading} type="primary" onClick={this.onSave}>
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
