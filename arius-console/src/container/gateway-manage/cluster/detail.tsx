import * as React from "react";
import { Button, Menu, PageHeader, Spin, Tag, Modal, message, Divider } from "knowdesign";
import "styles/detail.less";
import { GATEWAY_DESC_LIST, DETAIL_MENU_MAP, TAB_LIST } from "./config";
import Url from "lib/url-parser";
import { InfoItem } from "component/info-item";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getGatewayClusterDetail } from "api/gateway-manage";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { connect } from "react-redux";
import { StatusMap } from "constants/status-map";
import "./index.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const connects: Function = connect;
@connects(null, mapDispatchToProps)
export class GatewayClusterDetail extends React.Component<any> {
  public clusterId: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.id);
    window.addEventListener("hashchange", () => {
      this.updateLogicMenu();
    });
  }

  public state = {
    menu: window.location.hash.replace("#", "") || "node",
    clusterInfo: {} as IOpPhysicsClusterDetail,
    loading: false,
  };

  public updateLogicMenu() {
    this.setState({
      menu: window.location.hash.replace("#", "") || "node",
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
    getGatewayClusterDetail(this.clusterId)
      .then((res = {}) => {
        this.setState({
          clusterInfo: res,
        });
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  // public renderPageHeader() {
  //   const { clusterInfo } = this.state;
  //   return (
  //     <PageHeader
  //       className="detail-header"
  //       backIcon={false}
  //       title={clusterInfo?.clusterName || ""}
  //       // tags={
  //       //   <Tag className={`tag ${StatusMap[clusterInfo.health]}`} color={StatusMap[clusterInfo.health]}>
  //       //     {StatusMap[clusterInfo.health]}
  //       //   </Tag>
  //       // }
  //     >
  //       {GATEWAY_DESC_LIST.map((row, index) => {
  //         if (!clusterInfo) return null;
  //         if (row.key === "tags" && Number(JSON.parse(clusterInfo[row.key] || "{}")?.createSource) !== 0) {
  //           return null;
  //         }
  //         return (
  //           <InfoItem
  //             key={index}
  //             label={row.label}
  //             // @ts-ignore
  //             value={row.render ? row.render(clusterInfo?.[row.key]) : `${clusterInfo?.[row.key] || "-"}`}
  //             width={250}
  //           />
  //         );
  //       })}
  //     </PageHeader>
  //   );
  // }
  public renderPageHeader() {
    const { clusterInfo } = this.state;
    return (
      <div className="detail-header">
        <div className="left-content">
          <span className="icon iconfont iconarrow-left" onClick={() => this.props.history.push("/cluster/gateway")}></span>
          <Divider type="vertical"></Divider>
          <div className="title">
            <span className="text">{clusterInfo?.clusterName || ""}</span>
            <Tag className={`tag ${StatusMap[clusterInfo.health]}`} color={StatusMap[clusterInfo.health]}>
              {StatusMap[clusterInfo.health]}
            </Tag>
          </div>
        </div>
        <div className="right-content">
          {GATEWAY_DESC_LIST.map((row, index) => {
            if (!clusterInfo) return null;
            if (row.key === "tags" && Number(JSON.parse(clusterInfo[row.key] || "{}")?.createSource) !== 0) {
              return null;
            }
            return (
              <span className="detail" key={index}>
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

  public renderContent = () => {
    return DETAIL_MENU_MAP.get(this.state.menu)?.content({
      clusterInfo: this.state.clusterInfo,
      reloadData: this.reloadData,
      loading: this.state.loading,
      history: this.props.history,
    });
  };

  public changeMenu = (e) => {
    window.location.hash = e.key;
  };

  public render() {
    return (
      <div className="cluster-detail detail-container">
        <Spin spinning={this.state.loading}>
          {this.renderPageHeader()}
          <div className="content cluster-menu-container">
            <div className="menu-container cluster-menu">
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
