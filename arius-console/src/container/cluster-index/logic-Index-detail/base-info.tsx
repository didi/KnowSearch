import React from 'react';
import { Col, Row, } from 'antd';
import { IBaseInfo } from 'typesPath/base-types';
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
          <BaseDetail columns={baseInfo} baseDetail={data} />
        </>
    );
  }
}
