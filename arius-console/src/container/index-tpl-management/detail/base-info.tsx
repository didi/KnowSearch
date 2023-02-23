import React from "react";
import { baseInfo } from "./config";
import { BaseDetail } from "component/dantd/base-detail";
import "./index.less";

export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}
export class BaseInfo extends React.Component<{ data: any }> {
  constructor(props: any) {
    super(props);
  }

  public render() {
    const { data } = this.props;
    return (
      <div className="base-info">
        <BaseDetail columns={baseInfo} baseDetail={data} />
      </div>
    );
  }
}
