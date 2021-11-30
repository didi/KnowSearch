import * as React from 'react';
import Url from 'lib/url-parser';
import './index.less';
import { DCDR_STEPS } from './config';
import { DCDR_TASK_STATUS_TYPE_MAP } from 'constants/status-map';
import  { Divider, PageHeader, Steps }  from 'antd';

const task = {} as any;
const { Step } = Steps;

export class DcdrPlanSpeed extends React.Component {
  public id: number = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
  }

  public render() {
    let status = task.dcdrStepDetail.status ===  DCDR_TASK_STATUS_TYPE_MAP[1] ? 'error' : 'process' as any;
    if ((DCDR_STEPS.length - 1) === task.dcdrStepDetail.taskProgress) status = 'finish';
    return (
      <>
      <PageHeader
        className="detail-header"
        backIcon={false}
        title={'执行进度'}
      >
        <Steps
          direction="vertical"
          current={task.dcdrStepDetail.taskProgress}
          status={status}
        >
            {
              DCDR_STEPS.map((item, index) => {
                return <Step
                  key={index}
                  title={item.title}
                  description={status === index ? task.dcdrStepDetail?.comment : item.description}
                  icon={(task.dcdrStepDetail.status === DCDR_TASK_STATUS_TYPE_MAP[1] && task.dcdrStepDetail.taskProgress === index)
                  ? 'loading...'
                  : ''}
                />;
              })
            }
        </Steps>
        <Divider />
      </PageHeader>
      </>
    );
  }
}
