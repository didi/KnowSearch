import * as React from 'react';
import { BASE_INFO, ORDER_INFO } from './config';
import Url from 'lib/url-parser';
import { Descriptions, Spin, Divider } from 'antd';
import { IBaseInfo } from 'typesPath/base-types';
import { connect } from "react-redux";
import { TaskState } from 'store/type';

const mapStateToProps = state => ({
  task: state.task
});

const connects: Function = connect;
@connects(mapStateToProps)
export class BaseInfo extends React.Component<{ task: TaskState }> {
  public id: number = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
  }

  public render() {
    const { task } = this.props;
    return (
      <Spin spinning={task.loading}>
        <Descriptions title="基础信息" className="base-info" size="middle" column={3}>
          {BASE_INFO.map((item: IBaseInfo, index: number) => (
            <Descriptions.Item key={index} label={item.label}>
              {item.render ? item.render(task.taskBaseInfo?.[item.key]) : task.taskBaseInfo?.[item.key]}
            </Descriptions.Item>
          ))}
        </Descriptions>
        <Divider />
        {/* <Descriptions title="关联工单" className="base-info" size="middle" column={3}>
          {ORDER_INFO(task).map((item: IBaseInfo, index: number) => (
            <Descriptions.Item key={index} label={item.label}>
              {item.render ? item.render(task.taskBaseInfo?.[item.key]) : task.taskBaseInfo?.[item.key]}
            </Descriptions.Item>
          ))}
        </Descriptions> */}
      </Spin>
    );
  }
}
