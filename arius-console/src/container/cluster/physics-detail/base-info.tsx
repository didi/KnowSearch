import React from "react";
import { List, Switch, Tooltip } from "antd";
import {
  baseInfo,
  configInfo,
  indexExplain,
  onHandleServerTag,
} from "./config";
import Url from "lib/url-parser";
import {
  IOpPhysicsClusterDetail,
  ITemplateSrvData,
} from "@types/cluster/cluster-types";
import "./index.less";
import {
  getPhyClusterAvalibleTemplateSrv,
  getPhyClusterTemplateSrv,
} from "api/cluster-api";
import { BaseDetail } from "component/dantd/base-detail";
import { isOpenUp, showTag } from 'constants/common';
// 动态配置换成单独的tabs
// import { EditList } from "./edit-list";
export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}
export class ClusterInfo extends React.Component<{
  phyBaseInfo: IOpPhysicsClusterDetail;
}> {
  public clusterId: number;
  public physicsCluster: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
    this.physicsCluster = url.search.physicsCluster;
  }

  public state = {
    clusterTemplateSrvs: [],
    hasCapacityServer: false,
  };

  public componentDidMount() {
    this.reloadDataIndexServer();
  }

  public reloadDataIndexServer() {
    Promise.all([
      getPhyClusterAvalibleTemplateSrv(this.physicsCluster),
      getPhyClusterTemplateSrv(this.physicsCluster),
    ]).then((res) => {
        this.setPhyClusterTemplateSrvs(res);
    });
  }

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
      if(isOpenUp) {
        // 在开源环境下，分类为 0 隐藏
        if(showTag[item.serviceName] !== 0) {
          phyClusterTemplateSrvs.push({
            item,
            status: !!currentSrvs.find((row) => row.serviceId === item.serviceId) ? 1 : 0,
          });
        }
      } else {
        phyClusterTemplateSrvs.push({
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
    if(isOpenUp) {
      if (showTag[label] === 2) {
        return '能力开发中，敬请期待';
      }
      if (showTag[label] === 3) {
        return '该功能仅面向商业版客户开放';
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
    const { phyBaseInfo } = this.props;
    const { clusterTemplateSrvs } = this.state;
    return (
      <>
        <BaseDetail
          title={"基本信息"}
          columns={baseInfo}
          baseDetail={phyBaseInfo}
        />
        {/* <BaseDetail
          title={"配置信息"}
          columns={configInfo}
          baseDetail={phyBaseInfo}
        /> */}
        <div className="base-info-title">索引模板服务</div>
        <div className="base-index-box">
          <div className="base-index-box-tag">
            {clusterTemplateSrvs.map((row: ITemplateSrvData, index: number) => (
              <div className="base-index-box-tag-item" key={index}>
                <Tooltip
                  placement="top"
                  title={this.renderTip(row.item.serviceName)}
                  className="base-index-box-tag-item-title"
                >
                  {row.item.serviceName}
                </Tooltip>
                <Switch
                  disabled={true}
                  size="small"
                  checked={row.status ? true : false}
                  onClick={() =>
                    onHandleServerTag(
                      row,
                      phyBaseInfo.cluster,
                      this.reloadDataIndexServer.bind(this)
                    )
                  }
                />
              </div>
            ))}
          </div>
        </div>
        {/* <EditList /> */}
      </>
    );
  }
}
