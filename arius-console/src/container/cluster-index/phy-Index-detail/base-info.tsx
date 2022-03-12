import React from 'react';
import { Col, Row, } from 'antd';
import { IBaseInfo } from '@types/base-types';
import { baseInfo } from './config';
import './index.less';
import { BaseDetail } from 'component/dantd/base-detail';

export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}
export class BaseInfo extends React.Component<{data: any}> {

  constructor(props: any) {
    super(props);
  }


  public render() {
    const {data} = this.props;
    return (
        <>
          <BaseDetail title={'基本信息'} columns={baseInfo} baseDetail={data} />
          {/* <div className='base-info-title'>
            索引监控图表
          </div> */}
        </>
    );
  }
}
