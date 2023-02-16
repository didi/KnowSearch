import * as React from "react";
import { BasicInfo } from "./basicInfo";
import { Mapping } from "./mapping";
import "./index.less";
import { LastStep } from "./last-step";
import { Setting } from "./setting";
import Url from "lib/url-parser";
import { Steps } from "antd";
import { connect } from "react-redux";
import * as actions from "actions";
import { getFormatJsonStr } from "lib/utils";
import { getIndexDataType } from "api/cluster-index-api";

const { Step } = Steps;

const mapStateToProps = (state) => ({
  createIndex: state.createIndex,
});
const connects: Function = connect;

@connects(mapStateToProps)
export class CreateIndexTpl extends React.Component<any> {
  state = {
    dataTypeList: [],
  };

  constructor(props: any) {
    super(props);
    const url = Url();
    this.props.dispatch(
      actions.setCreateIndex({
        customerAnalysisValue: getFormatJsonStr({
          index: {
            "translog.durability": "async",
            "translog.sync_interval": "15s",
            refresh_interval: "1s",
            // codec: "default",
          },
        }),
      })
    );
  }

  public componentDidMount() {
    getIndexDataType().then((res = {}) => {
      const dataTypeList = Object.keys(res).map((key) => {
        return {
          title: res[key],
          label: res[key],
          value: Number(key),
        };
      });
      this.setState({ dataTypeList });
    });
  }

  public renderContent = () => {
    const componentMap = {
      0: <BasicInfo {...this.props} dataTypeList={this.state.dataTypeList} />,
      1: <Mapping />,
      2: <Setting />,
      3: <LastStep {...this.props} dataTypeList={this.state.dataTypeList} />,
    } as {
      [key: number]: JSX.Element;
    };
    return componentMap[this.props.createIndex.currentStep];
  };

  public render() {
    return (
      <>
        <div className="content-wrapper">
          <Steps current={this.props.createIndex.currentStep}>
            <Step title="基础信息" />
            <Step title="Mapping设置" />
            <Step title="Setting设置" />
            <Step title="设置完成" />
          </Steps>
          {this.renderContent()}
        </div>
      </>
    );
  }
}
