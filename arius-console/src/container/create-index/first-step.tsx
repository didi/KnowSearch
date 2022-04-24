import * as React from 'react';
import { XForm as XFormComponent, IFormItem } from 'component/x-form';
import { REGION_LIST } from 'constants/common';
import { getStepOneFormMap } from './config';
import { TEMP_FORM_MAP_KEY } from './constant';
import Url from 'lib/url-parser';
import { CancelActionModal } from 'container/custom-component';
import { Button, notification, Spin, Modal } from 'antd';
import { getIndexBaseInfo, queryQuotaCost, updateIndexInfo, getClusterTemplateSrv } from 'api/cluster-index-api';
import { connect } from "react-redux";
import * as actions from 'actions';
import { urlPrefix } from 'constants/menu';
import { dropByCacheKey } from 'react-router-cache-route';

const mapStateToProps = state => ({
  createIndex: state.createIndex
});

const connects: Function = connect;
@connects(mapStateToProps)
export class FirstStep extends React.Component<any> {
  public state = {
    clusterType: 1,
    cyclicalRoll: '',
    currentRegion: REGION_LIST[0].value,
    formData: null as any,
    loading: false,
  };
  private $formRef: any = null;
  private isModifyPage: boolean = false;
  private id: number = null;
  private clusterId: number = null;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
    this.clusterId = Number(url.search.clusterId);
    this.isModifyPage = window.location.pathname.includes('modify');
  };


  public async componentDidMount() {

    if (this.isModifyPage) {
      this.setState({
        loading: true,
      });
      getIndexBaseInfo(this.id).then((data) => {
        data.cyclicalRoll = data.cyclicalRoll ? 'more' : 'one';
        data.responsible = data.responsible?.split(',');
        data.department = data.libraDepartmentId;
        this.setState({
          formData: data,
          loading: false,
        });
      });
    }
  }

  public componentWillUnmount() {
    this.$formRef = null;
  }

  public onHandleValuesChange = (value: any, allValues: object) => {

    const checkIndexConfigByCluster = () => {
      if (value?.clusterInfo && value?.clusterInfo?.cluster) {
        getClusterTemplateSrv(value?.clusterInfo?.cluster).then((res) => {
          let disableHotTimeState = true;
        
          if (res && res.length) {
            res.forEach((item) => {
              if (item.serviceId === 8) {
                disableHotTimeState = false;
              }
            })
          }
          this.props.dispatch(actions.setCreateIndex({ disableHotTimeState }));
        })
      }
    }

    Object.keys(value).forEach(key => {
      switch (key) {
        case 'region':
          this.setState({
            currentRegion: value[key],
          });
          break;
        case 'cyclicalRoll':
          this.setState({
            [key]: value[key],
          }, () => {
            this.getCostData(allValues);
          });
          if (value[key] === 'more') {
            this.$formRef.setFieldsValue({ disableIndexRollover: 'false' });
          }
          break;
        case 'type':
          this.$formRef.setFieldsValue({ clusterName: null });
          break;
        case 'expireTime':
        case 'cluster':
        case 'quota':
          this.getCostData(allValues);
          break;
        case 'clusterInfo':
          this.$formRef.setFieldsValue({ level: value?.clusterInfo?.level, hotTime: '' }); // 选择集群后给level初始值
          checkIndexConfigByCluster();
          this.props.dispatch(actions.setCreateIndex({dataCenter: value?.clusterInfo?.dataCenter}));
          this.setState({ ...this.state });
          break;
      }
    });
  }

  public onSubmit = () => {
    this.$formRef.validateFields().then(result => {
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.firstStepFormData, result));
      // if ((result.cyclicalRoll === 'more' || result.dataType === 1) && !this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.tableMappingValues)) {
      //   this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.tableMappingValues, {
      //     'type_0_level-0-0': ['date', 'yyyy-MM-dd HH:mm:ss.SSS'],
      //     'name_0_level-0-0': 'logTime',
      //     'partition_0_level-0-0': true,
      //   }));
      // }
      this.props.dispatch(actions.setTemporaryFormMap(TEMP_FORM_MAP_KEY.isCyclicalRoll, result.cyclicalRoll === 'more'));
      this.props.dispatch(actions.setCurrentStep(1));
    }).catch(err => {
      return;
    });
  }

  public onSave = () => {
    const { formData } = this.state;
    this.$formRef.validateFields().then(result => {
      Object.assign(formData, {}, {
        id: this.id,
        desc: result.desc,
        responsible: result.responsible.join(),
        dataType: result.dataType,
      });
      updateIndexInfo(formData).then(() => {
        notification.success({ message: '更新成功' });
        window.setTimeout(() => {
          if (this.props?.history) {
            dropByCacheKey('index-tpl-management')
            this.props.history.push('/index-tpl-management');
          } else {
            window.location.href = `${urlPrefix}${'/index-tpl-management'}`;
          }
        }, 150);
      });
    });
  }

  public getCostData = (allValues: any) => {
    if (this.isModifyPage) {
      return;
    }
    const data = this.$formRef.getFieldsValue(['cluster', 'expireTime', 'quota', 'clusterType']);
    const clusterValue = allValues.clusterInfo?.cluster || data.cluster;
    const quota = allValues.quota || data.quota;
    const clusterType = allValues.clusterInfo?.clusterType || data.clusterType;
    const clusterId = !isNaN(this.clusterId) ? this.clusterId : clusterValue;
    const diskG = quota;

    if (clusterType === 2 || !clusterValue || !quota) {
      this.$formRef.setFieldsValue({ quotaMoney: '0 元/月' });
      return;
    }

    if (typeof diskG !== 'number' || diskG === 0) {
      return;
    }

    queryQuotaCost(diskG, clusterId).then((data) => {
      this.$formRef.setFieldsValue({ quotaMoney: data?.totalPrice?.toFixed(2) + '元/月' });
    });
  }

  public getFormMap = (formData: any): IFormItem[] => {
    const { cyclicalRoll: cyclicalRollFromStore, clusterType } = this.state;
    const finClusterType = isNaN(this.clusterId) ? clusterType : this.props.createIndex.myClusterList?.find(item => item.id === this.clusterId)?.type;
    const cyclicalRoll = cyclicalRollFromStore || formData.cyclicalRoll;

    if (this.isModifyPage && !formData.name) {
      return [];
    }
    return getStepOneFormMap(
      finClusterType,
      cyclicalRoll,
      this.isModifyPage,
      this.$formRef,
      this.props.createIndex.disableHotTimeState
    );
  }

  public clearStore = (str) => {
    this.props?.setRemovePaths([str]);
    dropByCacheKey('index/create');
    this.props.dispatch(actions.setClearCreateIndex());
  }


  public render() {
    const formData = this.state.formData || this.props.createIndex.temporaryFormMap.get(TEMP_FORM_MAP_KEY.firstStepFormData) || {};

    if (this.isModifyPage && !formData.clusterInfo) {
      formData.clusterInfo = {
        cluster: formData.cluster,
        clusterName: formData.cluster,
        clusterType: formData.clusterType
      }
    }

    return (
      <>
        <Spin spinning={this.state.loading}>
          <div className={this.isModifyPage ? 'content-wrapper edit' : 'step'}>
            <XFormComponent
              wrappedComponentRef={(formRef) => this.$formRef = formRef}
              formData={formData}
              formMap={this.getFormMap(formData)}
              onHandleValuesChange={this.onHandleValuesChange}
            />
            <div className="op-btn-group">
              {this.isModifyPage && <Button type="primary" onClick={this.onSave}>保存</Button>}
              {!this.isModifyPage && <Button type="primary" onClick={this.onSubmit}>下一步</Button>}
              <CancelActionModal routeHref={'/index-tpl-management'} history={this.props?.history} cb={this.clearStore} />
            </div>
          </div>
        </Spin>
      </>
    );
  }
}
