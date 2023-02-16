import React from "react";
import { baseInfo } from "./config";
import Url from "lib/url-parser";
import { IClusterInfo } from "typesPath/cluster/cluster-types";
import "./index.less";
import { BaseDetail } from "component/dantd/base-detail";

export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}
export class ClusterInfo extends React.Component<{ logicBaseInfo: IClusterInfo }> {
  public clusterId: number;
  public clusterName: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
    this.clusterName = url.search.physicsCluster;
  }

  public render() {
    const { logicBaseInfo } = this.props;
    return (
      <div className="base-info">
        <BaseDetail columns={baseInfo} baseDetail={logicBaseInfo} />
      </div>
    );
  }
}
