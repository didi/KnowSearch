import * as React from 'react';
import { FirstStep } from './first-step';
import { SecondStep } from './second-step';
import './index.less';
import { ThirdStep } from './third-step';
import Url from 'lib/url-parser';
import { Steps }  from 'antd';
import { connect } from "react-redux";

const { Step } = Steps;

const mapStateToProps = state => ({
  createIndex: state.createIndex
});

@connect(mapStateToProps)
export class CreateIndex extends React.Component<any> {
  constructor(props: any) {
    super(props);
    const url = Url();
  }

  public renderContent = () => {
    const componentMap = {
      0: <FirstStep {...this.props}/>,
      1: <SecondStep />,
      2: <ThirdStep {...this.props}/>,
    } as {
      [key: number]: JSX.Element,
    };
    return componentMap[this.props.createIndex.currentStep];
  }


  public render() {
    return (
      <>
        <div className="content-wrapper">
          <Steps current={this.props.createIndex.currentStep}>
            <Step title="基本信息" />
            <Step title="索引结构" />
            <Step title="设置完成" />
          </Steps>
          {this.renderContent()}
        </div>
      </>
    );
  }
}
