import React from 'react';
import { Col, Empty, Switch, Tooltip }  from 'antd';
import { IBaseInfo } from 'typesPath/base-types';
import { baseInfo, indexExplain, onHandleServerTag } from './config';
import Url from 'lib/url-parser';
import { IClusterInfo, ITemplateSrvData } from 'typesPath/cluster/cluster-types';
import './index.less';
import {  getLogicClusterAvalibleTemplateSrv, getLogicClusterTemplateSrv } from 'api/cluster-api';
import { BaseDetail } from 'component/dantd/base-detail';
import { isOpenUp, showTag } from 'constants/common';

export interface ITemplateSrv {
  esVersion: string;
  serviceId: number;
  serviceName: string;
}
export class ClusterInfo extends React.Component<{logicBaseInfo: IClusterInfo}> {
  public clusterId: number;
  public clusterName: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
    this.clusterName = url.search.physicsCluster;
  }

  public state = {
    clusterTemplateSrvs: [],
    hasCapacityServer: false,
  }

  public componentDidMount() {
    this.reloadDataIndexServer();
  }

  public reloadDataIndexServer() {
    const { id } = this.props.logicBaseInfo;
    if (!id) return;
      Promise.all([getLogicClusterAvalibleTemplateSrv(id), getLogicClusterTemplateSrv(id)]).then((res) => {
        this.setPhyClusterTemplateSrvs(res);
      });
  }

  public componentWillReceiveProps() {
    this.reloadDataIndexServer();
  }

  public componentWillUnmount() {
    this.setState = () => false;
  }

  public setPhyClusterTemplateSrvs = (data: ITemplateSrv[][]) => {
    const avalibleSrvs = data?.[0] || [];
    const currentSrvs = data?.[1] || [];
    const phyClusterTemplateSrvs = [];
    const { authType } = this.props.logicBaseInfo;
    // 是否拥有容量规划服务
    const hasCapacityServer = !!currentSrvs.find(row => row.serviceId === 11);

    for (const item of avalibleSrvs) {
      let disabled = false;
      if(authType !== 1) {
        disabled = true;
      } else if(isOpenUp) {
        // 开源环境部分功能禁用
        if (showTag[item.serviceName] === 2 || showTag[item.serviceName] === 3) {
          disabled = true
        }
      }

      if(isOpenUp) {
        // 在开源环境下，分类为 0 隐藏
        if(showTag[item.serviceName] !== 0) {
          phyClusterTemplateSrvs.push({
            item,
            disabled,
            status: !!currentSrvs.find(row => row.serviceId === item.serviceId) ? 1 : 0,
          });
        }
      } else {
        phyClusterTemplateSrvs.push({
          item,
          disabled,
          status: !!currentSrvs.find(row => row.serviceId === item.serviceId) ? 1 : 0,
        });
      }
    }

    this.setState({
      clusterTemplateSrvs: phyClusterTemplateSrvs,
      hasCapacityServer: hasCapacityServer
    })
  }

  public renderTip = (label: string) => {
    let tip = '暂无相关信息';
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
    if (this.props.logicBaseInfo.authType !== 1) {
      tip = '请联系该集群负责人操作';
    }
    return tip;
  }

  public render() {
    const {logicBaseInfo} = this.props;
    const {clusterTemplateSrvs} = this.state;
    return (
        <>
          <BaseDetail columns={baseInfo} baseDetail={logicBaseInfo} />
          <div className='base-index-box'>
            <div className='base-index-box-title'>
              索引模板服务
            </div>
            <div className='base-index-box-tag'>
                {clusterTemplateSrvs.map((row: ITemplateSrvData, index: number) => (
                  <div className='base-index-box-tag-item' key={index}>
                    <Tooltip title={this.renderTip(row.item.serviceName)} className='base-index-box-tag-item-title'>
                      <span style={{ color:  'rgb(0, 0, 0, 0.3)', cursor: 'not-allowed' }}>{row.item.serviceName}</span>
                    </Tooltip>
                    <Switch disabled={true} size="small" checked={row.status ? true : false} onClick={() => onHandleServerTag(row, logicBaseInfo.id, this.reloadDataIndexServer.bind(this))} />
                  </div>
                ))}
                {clusterTemplateSrvs && clusterTemplateSrvs.length < 1 ? <Empty style={{ width: '100%', margin: '20px 0px' }} description="该集群版本不支持索引模板服务"/> : null}
            </div>
          </div>
        </>
    );
  }
}
