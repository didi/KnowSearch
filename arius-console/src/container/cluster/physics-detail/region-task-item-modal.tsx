import * as React from 'react';
import moment from 'moment';
import { Row, Col } from 'antd';
import { IRegionTaskItem } from '@types/cluster/physics-type';

export const TaskItemModal = (props: IRegionTaskItem) => {
    return (
      <>
        <Row>
          <Col span={12}>
              <label>id： {props.id}</label>
          </Col>
          <Col span={12}>
            <label>物理模板id： {props.physicalId}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
              <label>集群： {props.cluster}</label>
          </Col>
          <Col span={12}>
            <label>模板名字：{props.templateName}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>数据保存时长： {props.expireTime}</label>
          </Col>
          <Col span={12}>
            <label>shard个数：{props.shardNum}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>模板创建时间： {moment(props.createTime).format('YYYY-MM-DD  hh:mm:ss')}</label>
          </Col>
          <Col span={12}>
            <label>时间后缀：: {props.dateFormat}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>模板总的磁盘消耗： {props.sumIndexSizeG}</label>
          </Col>
          <Col span={12}>
            <label>总条数： {props.sumDocCount}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>冷数据的天数： {''}</label>
          </Col>
          <Col span={12}>
            <label>tps峰值：: {props.maxTps}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>查询的峰值： {props.maxQueryTime}</label>
          </Col>
          <Col span={12}>
            <label>scroll的峰值：{props.maxScrollTime}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>副本个数： {props.replicaNum}</label>
          </Col>
          <Col span={12}>
            <label>实际的磁盘消耗：{props.actualDiskG}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>实际的CPU消耗： {props.actualCpuCount}</label>
          </Col>
          <Col span={12}>
            <label>Quota的磁盘消耗：{props.quotaDiskG}</label>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <label>Quota的CPU消耗： {props.quotaCpuCount}</label>
          </Col>
          <Col span={12}>
            <label>综合的磁盘消耗：{props.combinedDiskG}</label>
          </Col>
        </Row>
      </>
    );
}
