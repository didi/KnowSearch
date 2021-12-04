import * as React from "react";
import { Button, Menu, PageHeader, Spin, Tag, Modal, message } from "antd";
import "styles/detail.less";
import { DESC_LIST, DETAIL_MENU_MAP, TAB_LIST } from "./config";
import Url from "lib/url-parser";
import { InfoItem } from "component/info-item";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getPhysicsClusterDetail } from "api/cluster-api";
import { IOpPhysicsClusterDetail } from "@types/cluster/cluster-types";
import { connect } from "react-redux";
import { InfoCircleOutlined } from "@ant-design/icons";
import { renderMoreBtns } from "container/custom-component";
import { getPhysicsBtnList } from "../config";
import {
  StatusMap,
} from "constants/status-map";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setDrawerId(modalId, params)),
});

const connects: Function = connect;
@connects(null, mapDispatchToProps)
export class PhyClusterDetail extends React.Component<any> {
  public clusterId: number;
  public auth: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.physicsClusterId);
    this.auth = Number(url.search.auth);
    window.addEventListener("hashchange", () => {
      this.updateLogicMenu();
    });
  }

  public state = {
    menu: window.location.hash.replace("#", "") || "info",
    clusterInfo: {} as IOpPhysicsClusterDetail,
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
    getPhysicsClusterDetail(this.clusterId)
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

  public getOpBtns = () => {
    const arr = getPhysicsBtnList(
      this.state.clusterInfo as any,
      this.props.setModalId,
      this.props.setDrawerId,
      this.reloadData
    );
    return renderMoreBtns(arr, this.state.clusterInfo);
  };

  public renderPageHeader() {
    const { clusterInfo } = this.state;
    return (
      <PageHeader
        className="detail-header"
        backIcon={false}
        title={clusterInfo?.cluster || ""}
        tags={
          <Tag color={StatusMap[clusterInfo.health]}>
            {StatusMap[clusterInfo.health]}
          </Tag>
        }
        extra={
          this.state.clusterInfo?.currentAppAuth === 1 || this.state.clusterInfo?.currentAppAuth === 0 ? this.getOpBtns() : null
        }
      >
        {DESC_LIST.map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={
              row.render
                ? row.render(clusterInfo?.[row.key])
                : `${clusterInfo?.[row.key] || ""}`
            }
            width={250}
          />
        ))}
      </PageHeader>
    );
  }

  public renderContent = () => {
    return DETAIL_MENU_MAP.get(this.state.menu)?.content(
      this.state.clusterInfo
    );
  };

  public changeMenu = (e) => {
    window.location.hash = e.key;
  };

  public render() {
    return (
      <Spin spinning={this.state.loading}>
        {this.renderPageHeader()}
        <Menu
          selectedKeys={[this.state.menu]}
          mode="horizontal"
          onClick={this.changeMenu}
        >
          {this.state.clusterInfo?.currentAppAuth === 1 ||
          this.state.clusterInfo?.currentAppAuth === 0
            ? TAB_LIST.map((d) => <Menu.Item key={d.key}>{d.name}</Menu.Item>)
            : null}
        </Menu>
        <div className="detail-wrapper">{this.renderContent()}</div>
      </Spin>
    );
  }
}
