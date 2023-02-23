import * as React from "react";
import { Menu, Spin, Divider } from "knowdesign";
import "styles/detail.less";
import { DESC_LIST, DETAIL_MENU_MAP, INDEX_TAB_LIST } from "./config";
import Url from "lib/url-parser";
import { Dispatch } from "redux";
import * as actions from "actions";
import { connect } from "react-redux";
import { getIndexBaseInfo } from "api/cluster-index-api";
import { IUNSpecificInfo } from "typesPath/base-types";
import { ITemplateLogic } from "typesPath/cluster/physics-type";
import { AppState } from "store/type";

export interface IBaseButton {
  label: string;
  type: "primary" | "dashed";
  clickFunc: () => any;
  attr?: any;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
});

const connects: Function = connect;
@connects(mapStateToProps, mapDispatchToProps)
export class IndexTplDetail extends React.Component<{ setModalId: Function; app: AppState; history: any }> {
  public id: number;
  public authType: number;
  private isServiceDetailPage: boolean = false;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
    this.authType = Number(url.search.authType);
    this.isServiceDetailPage = window.location.pathname.includes("/service");

    window.addEventListener("hashchange", () => {
      this.updateLogicMenu();
    });
  }

  public state = {
    menu: window.location.hash.replace("#", "") || "info",
    indexBaseInfo: {} as ITemplateLogic,
    loading: false,
  };

  public updateLogicMenu() {
    this.setState({
      menu: window.location.hash.replace("#", "") || "info",
    });
  }

  public componentDidMount() {
    this.reloadData();
  }

  public componentWillUnmount() {
    this.setState = () => false;
  }

  public reloadData = () => {
    this.setState({
      loading: true,
    });
    getIndexBaseInfo(this.id)
      .then((baseInfo: IUNSpecificInfo) => {
        const info = baseInfo || ({} as IUNSpecificInfo);
        info.first = info.indices?.[0];
        info.last = info.indices?.pop();
        info.authType = this.authType;
        info.expireTime = info.expireTime === -1 ? "永久" : `${info.expireTime}天`;
        this.setState({
          indexBaseInfo: info,
        });
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  public renderPageHeader() {
    const { indexBaseInfo } = this.state;
    return (
      <div className="detail-header">
        <div className="left-content">
          <span className="icon iconfont iconarrow-left" onClick={() => this.props.history.push("/index-tpl/service")}></span>
          <Divider type="vertical"></Divider>
          <div className="title">
            <span className="text">{indexBaseInfo?.name || ""}</span>
          </div>
        </div>
        <div className="right-content">
          {DESC_LIST.map((row, index) => {
            return (
              <span className="detail" key={index}>
                <span className="label">{row.label}：</span>
                <span className="value">{row.render(indexBaseInfo?.[row.key])}</span>
              </span>
            );
          })}
          <div className="reload-icon" onClick={this.reloadData}>
            <span className="icon iconfont iconshuaxin2"></span>
          </div>
        </div>
      </div>
    );
  }

  public renderContent = () => {
    return DETAIL_MENU_MAP.get(this.state.menu)?.content(this.state.indexBaseInfo);
  };

  public changeMenu = (e) => {
    window.location.hash = e.key;
  };

  public render() {
    return (
      <div className="detail-container">
        <Spin spinning={this.state.loading}>
          {this.renderPageHeader()}
          <div className="menu-container">
            <Menu className="menu" selectedKeys={[this.state.menu]} mode="horizontal" onClick={this.changeMenu}>
              {INDEX_TAB_LIST.map((d) => (
                <Menu.Item key={d.key}>{d.name}</Menu.Item>
              ))}
            </Menu>
          </div>

          <div className={`detail-wrapper ${this.isServiceDetailPage ? "index-tpl-detail service" : "index-tpl-detail"}`}>
            {this.renderContent()}
          </div>
        </Spin>
      </div>
    );
  }
}
