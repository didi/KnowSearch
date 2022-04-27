import * as React from "react";
import { Menu, PageHeader, Spin, Tag } from 'antd';
import "styles/detail.less";
import { DESC_LIST, DETAIL_MENU_MAP, INDEX_TAB_LIST } from "./config";
import Url from "lib/url-parser";
import { InfoItem } from "component/info-item";
import { Dispatch } from "redux";
import * as actions from "actions";
import { connect } from "react-redux";
import { getIndexBaseInfo } from "api/cluster-index-api";
import { IUNSpecificInfo } from "typesPath/base-types";
import { getBtnLogicIndexList } from "../config";
import { ITemplateLogic } from "typesPath/cluster/physics-type";
import { AppState } from "store/type";
import { renderMoreBtns } from "container/custom-component";
// import { delLogicCluster } from '../config';


export interface IBaseButton {
  label: string;
  type: "primary" | "dashed";
  clickFunc: () => any;
  attr?: any;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
});

const connects: Function = connect;
@connects(mapStateToProps, mapDispatchToProps)
export class LogicIndexDetail extends React.Component<{setModalId: Function, app: AppState, history: any }> {
  public id: number;
  public authType: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
    this.authType = Number(url.search.authType);
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
        info.responsible = baseInfo?.responsible?.split(",");
        info.first = info.indices?.[0];
        info.last = info.indices?.pop();
        info.authType = this.authType;
        info.expireTime =
          info.expireTime === -1 ? "永久" : `${info.expireTime}/天`;
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

  public getOpBtns = () => {
    const arr = getBtnLogicIndexList(
      this.state.indexBaseInfo,
      this.id,
      this.props.setModalId,
      this.reloadData,
      this.props.app.appInfo()?.id,
      this.props.history
    );
    return renderMoreBtns(arr, {});
  };

  public renderPageHeader() {
    const { indexBaseInfo } = this.state;
    return (
      <PageHeader
        className="detail-header"
        backIcon={false}
        title={indexBaseInfo.name || ""}
        extra={
          this.getOpBtns()
          // this.getOpBtns().map((item, index) => (<Button type={item.type} {...item.attr} key={index} onClick={item.clickFunc}>{item.label}</Button>))
        }
      >
        {DESC_LIST.map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={
              row.render
                ? row.render(indexBaseInfo?.[row.key], indexBaseInfo)
                : `${indexBaseInfo?.[row.key] || ""}`
            }
            width={row?.width || 200}
          />
        ))}
      </PageHeader>
    );
  }

  public renderContent = () => {
    return DETAIL_MENU_MAP.get(this.state.menu)?.content(
      this.state.indexBaseInfo
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
          {INDEX_TAB_LIST.map((d) => (
            <Menu.Item key={d.key}>{d.name}</Menu.Item>
          ))}
        </Menu>
        <div className="detail-wrapper">{this.renderContent()}</div>
      </Spin>
    );
  }
}
