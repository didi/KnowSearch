import * as React from "react";
import { Button, Menu, PageHeader, Spin, Tag } from 'antd';
import "styles/detail.less";
import { DESC_LIST, DETAIL_MENU_MAP, TAB_LIST } from "./config";
import Url from "lib/url-parser";
import { InfoItem } from "component/info-item";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getOpLogicClusterInfo } from "api/cluster-api";
import { IClusterInfo } from "typesPath/cluster/cluster-types";
import { connect } from "react-redux";
import { delLogicCluster } from "../config";
import {
  StatusMap,
} from "constants/status-map";
import { isOpenUp } from "constants/common";


export interface IBaseButton {
  label: string;
  isOpenUp?: boolean,
  type: "primary" | "dashed";
  clickFunc: () => any;
  attr?: any;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});
const connects: Function = connect
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

  public getOpBtns = (): IBaseButton[] => {
    if (this.state.clusterInfo.permissions !== "配置管理") return [];
    return [
      {
        label: "编辑",
        type: "primary",
        clickFunc: () =>
          this.props.setModalId(
            "editCluster",
            this.state.clusterInfo,
            this.reloadData
          ),
      },
      {
        label: "扩缩容",
        type: "primary",
        isOpenUp,
        clickFunc: () =>
          this.props.setModalId(
            "expandShrink",
            this.state.clusterInfo,
            this.reloadData
          ),
      },
      {
        label: "转让",
        type: "primary",
        isOpenUp,
        clickFunc: () =>
          this.props.setModalId(
            "transferCluster",
            this.state.clusterInfo,
            this.reloadData
          ),
      },
      {
        label: "删除",
        type: "primary",
        clickFunc: () => {
          delLogicCluster(this.state.clusterInfo as any, null, this.props.setModalId, () => {this.props?.history?.push('/cluster/logic')});
        },
      },
    ];
  };

  public renderPageHeader() {
    const { clusterInfo } = this.state;
    return (
      <PageHeader
        className="detail-header"
        backIcon={false}
        title={clusterInfo?.name || ""}
        tags={
          <Tag color={StatusMap[clusterInfo?.health]}>
            {StatusMap[clusterInfo?.health]}
          </Tag>
        }
        extra={this.getOpBtns().map((item, index) => (
          <Button
            type={item.type}
            {...item.attr}
            key={index}
            disabled={item.isOpenUp}
            onClick={item.clickFunc}
          >
            {item.label}
          </Button>
        ))}
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
            width={200}
          />
        ))}
      </PageHeader >
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
          {TAB_LIST.map((d) => (
            <Menu.Item key={d.key}>{d.name}</Menu.Item>
          ))}
        </Menu>
        <div className="detail-wrapper">{this.renderContent()}</div>
      </Spin>
    );
  }
}
