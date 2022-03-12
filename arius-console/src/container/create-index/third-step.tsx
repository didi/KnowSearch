import * as React from 'react';
import './index.less';
import { MappingInfoPreview } from './mapping-info-preview';
import { EditorCom } from 'component/editor';
import { TEMP_FORM_MAP_KEY, MAPPING_TYPE } from './constant';
import { getTableMappingData, getJsonMappingData } from './config';
import Url from 'lib/url-parser';
import { CancelActionModal } from 'container/custom-component';
import { Tabs, Button, notification } from 'antd';
import * as actions from 'actions';
import { connect } from "react-redux";
import { submitWorkOrder } from 'api/common-api';
import { getCookie, goToTargetPage } from 'lib/utils';
import store from 'store';

const mapStateToProps = state => ({
  createIndex: state.createIndex
});

@connect(mapStateToProps)
export class ThirdStep extends React.Component<any> {
  public extraInfo = {};
  private clusterId: number = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
    this.getExtraInfo();
  }

  public getExtraInfo = () => {
    const mappingType = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.mappingType);
    const firstStepFormData = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.firstStepFormData) || {};

    if (mappingType === MAPPING_TYPE.table) {
      getTableMappingData(firstStepFormData);
    }

    if (mappingType === MAPPING_TYPE.json) {
      getJsonMappingData(firstStepFormData);
    }

    this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.firstStepFormData, firstStepFormData));
  }

  public clearStore = () => {
    this.props.dispatch(actions.setClearCreateIndex());
  }

  public onSubmit = () => {
    const mappingData = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.mappingValue) || '';
    const formData = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.firstStepFormData);
    const type = 'templateCreate';
    console.log(mappingData);
    const postData = {
      name: `${formData.name}`, // 暂时cn TODO：
      dataCenter: 'cn',
      dataType: formData.dataType,
      // libraDepartment: department.departmentList.find(row => row.idAlias === formData.department)?.name,
      // libraDepartmentId: formData.department,
      responsible: formData.responsible.join(),
      desc: formData.desc,
      expireTime: formData.cyclicalRoll === 'more' ? formData.expireTime : -1,
      cyclicalRoll: formData.cyclicalRoll === 'more' ? 1 : 0,
      diskQuota: Number(formData.quota),
      mapping: mappingData,
      resourceId: formData.clusterInfo?.cluster, // TODO: 判断表单和url传值是否一致？
      cluster: formData.clusterInfo?.clusterName, // TODO: 判断表单和url传值是否一致？
      idField: formData.idField,
      dateField: formData.dateField,
      routingField: formData.routingField,
      dateFieldFormat: formData.dateFieldFormat,
    };
    const backUrl = '/index-tpl-management';
    // this.props.dispatch(actions.setLoadingMap('create-loading', true));
    submitWorkOrder({
      contentObj: postData,
      submitorAppid: store.getState().app.appInfo()?.id,
      submitor: getCookie('domainAccount') || '',
      description: formData.description,
      type,
    }, () => {
      // this.props.dispatch(actions.setLoadingMap('create-loading', false));
      // TODO:: 跳转
      this.props?.history?.push(backUrl);
      this.clearStore();
      // goToTargetPage(backUrl);
    });
  }

  public render() {
    const value = this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.thirdStepPreviewJson) || '';
    const url = '/'

    return (
      <>
        <Tabs type="card" className="tab-content">
          <Tabs.TabPane tab="基本信息" key="1">
            <MappingInfoPreview />
          </Tabs.TabPane>
          <Tabs.TabPane tab="索引结构" key="2">
            <div className="json-content-wrapper">
              <EditorCom
                options={{
                  language: 'json',
                  value,
                  theme: 'vs',
                  automaticLayout: true,
                  readOnly: true,
                }}
              />
            </div>
          </Tabs.TabPane>
        </Tabs>
        <div className="op-btn-group">
          <Button onClick={() => this.props.dispatch(actions.setCurrentStep(1))}>上一步</Button>
          <Button type="primary" loading={this.props.createIndex.loadingMap['create-loading']} onClick={this.onSubmit}>提交申请</Button>
          <CancelActionModal routeHref={url} history={this.props?.history} cb={this.clearStore}/>
        </div>
      </>
    );
  }
}
