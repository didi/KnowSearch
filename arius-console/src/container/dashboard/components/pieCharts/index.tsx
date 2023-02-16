import React from "react";
import { PieChart } from "./pieChart";
import { Spin } from "antd";
import { dashboardHealth } from "api/dashboard";

interface Istate {
  dataSource: any;
  loading: boolean;
}

export class PieCharts extends React.Component<any, Istate> {
  public constructor(props) {
    super(props);
  }

  public state: Istate = {
    dataSource: {},
    loading: true,
  };

  public getData = () => {
    // const { startTime, endTime } = this.props;
    this.setState({ dataSource: {}, loading: true });
    dashboardHealth()
      .then((res) => {
        this.setState({ dataSource: res, loading: false });
      })
      .catch((err) => {
        this.setState({ loading: false });
      });
  };

  public render() {
    const { dataSource, loading } = this.state;
    return (
      <Spin spinning={loading}>
        <div className="piedashboardbox">
          <PieChart type="dashboard" dataSource={dataSource} loading={loading} dictionary={this.props?.dictionary?.health} />
        </div>
      </Spin>
    );
  }
}
