import React from "react";
import { baseInfo, indexExplain } from "./config";
import Url from "lib/url-parser";
import "./index.less";
import { BaseDetail } from "component/dantd/base-detail";
import { isOpenUp, showTag } from "constants/common";
import { Dispatch } from "redux";
import * as actions from "actions";
import { connect } from "react-redux";

// 动态配置换成单独的tabs
// import { EditList } from "./edit-list";
export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params)),
});
const connects: Function = connect;
@connects(null, mapDispatchToProps)
export class ClusterInfo extends React.Component<any> {
  public clusterId: number;
  public physicsCluster: string;
  public auth: string;
  constructor(props: any) {
    super(props);
    const url = Url();
    this.auth = url.search.auth;
    this.clusterId = Number(url.search.clusterId);
    this.physicsCluster = url.search.physicsCluster;
  }

  public state = {
    clusterTemplateSrvs: [],
    hasCapacityServer: false,
  };

  public componentWillUnmount() {
    this.setState = () => false;
  }

  public setPhyClusterTemplateSrvs = (data: ITemplateSrv[][]) => {
    const avalibleSrvs = data?.[0] || [];
    const currentSrvs = data?.[1] || [];
    const phyClusterTemplateSrvs = [];
    // 是否拥有容量规划服务
    const hasCapacityServer = !!currentSrvs.find((row) => row.serviceId === 11);

    for (const item of avalibleSrvs) {
      // -1 -> 无权限
      let disabled = this.auth === "-1" ? true : false;
      if (isOpenUp) {
        // 开源环境部分功能禁用
        if (showTag[item.serviceName] === 3) {
          disabled = true;
        }
        // 在开源环境下，分类为 0 隐藏
        if (showTag[item.serviceName] !== 0) {
          phyClusterTemplateSrvs.push({
            disabled,
            item,
            status: !!currentSrvs.find((row) => row.serviceId === item.serviceId) ? 1 : 0,
          });
        }
      } else {
        phyClusterTemplateSrvs.push({
          disabled,
          item,
          status: !!currentSrvs.find((row) => row.serviceId === item.serviceId) ? 1 : 0,
        });
      }
    }
    this.setState({
      clusterTemplateSrvs: phyClusterTemplateSrvs,
      hasCapacityServer: hasCapacityServer,
    });
  };

  public renderTip = (label: string) => {
    let tip = "暂无相关信息";
    if (isOpenUp) {
      if (showTag[label] === 3) {
        return "该功能仅面向商业版客户开放";
      }
    }
    indexExplain.forEach((item) => {
      if (item.label === label) {
        tip = item.content;
      }
    });
    return tip;
  };

  public render() {
    const { phyBaseInfo, setModalId, reloadData } = this.props;
    return (
      <div className="base-info">
        <BaseDetail columns={baseInfo(setModalId, reloadData, phyBaseInfo)} baseDetail={phyBaseInfo} />
      </div>
    );
  }
}
