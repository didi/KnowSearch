import * as React from "react";
import { Menu, Spin, Tag, Divider } from "knowdesign";
import { DESC_LIST, TAB_LIST, DETAIL_MENU_MAP } from "./config";
import Url from "lib/url-parser";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getOpLogicClusterInfo } from "api/cluster-api";
import { IClusterInfo } from "typesPath/cluster/cluster-types";
import { connect } from "react-redux";
import { StatusMap } from "constants/status-map";
import "styles/detail.less";
import "./index.less";

export interface IBaseButton {
  label: string;
  isOpenUp?: boolean;
  type: "primary" | "dashed";
  clickFunc: () => any;
  attr?: any;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});
const connects: Function = connect;
@connects(null, mapDispatchToProps)
export class LogicClusterDetail extends React.Component<any> {
  public clusterId: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
    window.addEventListener("hashchange", () => {
      this.updateLogicMenu();
    });
  }

  public state = {
    menu: window.location.hash.replace("#", "") || "info",
    clusterInfo: {} as IClusterInfo,
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
    getOpLogicClusterInfo(this.clusterId)
      .then((res) => {
        if (res) {
          this.setState({
            clusterInfo: res,
          });
        }
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  public renderPageHeader() {
    const { clusterInfo } = this.state;

    return (
      <div className="detail-header">
        <div className="left-content">
          <span className="icon iconfont iconarrow-left" onClick={() => this.props.history.push("/cluster/logic")}></span>
          <Divider type="vertical"></Divider>
          <div className="title">
            <span className="text">{clusterInfo?.name || ""}</span>
            <Tag className={`tag ${StatusMap[clusterInfo.health]}`} color={StatusMap[clusterInfo.health]}>
              {StatusMap[clusterInfo.health]}
            </Tag>
          </div>
        </div>
        <div className="right-content">
          {DESC_LIST.map((row, index) => {
            if (!clusterInfo) return null;
            if (row.key === "tags" && Number(JSON.parse(clusterInfo[row.key] || "{}")?.createSource) !== 0) {
              return null;
            }
            return (
              <span className="cluster-detail" key={index}>
                <span className="label">{row.label}：</span>
                {/* @ts-ignore */}
                <span className="value">{row.render ? row.render(clusterInfo?.[row.key]) : `${clusterInfo?.[row.key] || "-"}`}</span>
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

  public changeMenu = (e) => {
    window.location.hash = e.key;
  };

  public renderContent = () => {
    return DETAIL_MENU_MAP.get(this.state.menu)?.content({
      clusterInfo: this.state.clusterInfo,
      reloadData: this.reloadData,
      loading: this.state.loading,
    });
  };

  public render() {
    return (
      <div className="detail-container">
        <Spin spinning={this.state.loading}>
          {this.renderPageHeader()}
          <div className="content">
            <div className="menu-container">
              <Menu className="menu" selectedKeys={[this.state.menu]} mode="horizontal" onClick={this.changeMenu}>
                {TAB_LIST.map((d) => (
                  <Menu.Item key={d.key}>{d.name}</Menu.Item>
                ))}
              </Menu>
            </div>
            <div className="detail-wrapper">{this.renderContent()}</div>
          </div>
        </Spin>
      </div>
    );
  }
}
