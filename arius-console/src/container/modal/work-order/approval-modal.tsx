import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { FORCED_EXPANSION_MAP } from 'constants/status-map';
import { approvalOrder } from 'api/order-api';
import { IApprovalOrder, IOrderInfo } from '@types/cluster/order-types';
import { AppState, UserState } from 'store/type';
import { notification } from 'antd';
import { PhyClusterRacks, AllPhyCluster } from 'container/custom-form/region';

const mapStateToProps = state => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

export const ShowApprovalModal = connect(mapStateToProps)((props: {params: IOrderInfo, dispatch: Function, app: AppState, cb: Function, user: UserState}) => {
  const { id, type, detailInfo: { clusterPhyNameList = [] }} = props.params;

  const formtTemplateCreateMap = [{
    key: 'cluster',
    label: '物理集群名称',
    type: FormItemType.custom,
    customFormItem: <AllPhyCluster clusterPhyNameList={clusterPhyNameList} />,
    rules: [{
      required: true,
      message: '请输入物理集群名称',
    }],
  }, {
    key: 'rack',
    label: 'rack信息',
    type: FormItemType.custom,
    customFormItem: <PhyClusterRacks />,
    rules: [{
      required: true,
      message: '请输入rack信息',
    }],
  }] as any;

  const formtTemplateIndecreaseMap = [{
    key: 'force',
    label: '强制扩容',
    type: FormItemType.select,
    options: FORCED_EXPANSION_MAP,
    rules: [{ required: true, message: '请选择' }],
  }] as any;

  const xFormModalConfig = {
    formMap: [
      {
        key: 'id',
        label: '工单ID',
        type: FormItemType.inputNumber,
        attrs: { disabled: true },
      },
      {
        key: 'opinion',
        label: '审批意见',
        type: FormItemType.textArea,
        rules: [{
          required: true,
          whitespace: true,
          validator: async (rule: any, value: string) => {
              if (value && value.length > 100) {
                return Promise.reject('请输入1-100字审批意见');
              }
              if (value === '' || !value) {
                 return Promise.reject('请输入1-100字审批意见');
              }
              return Promise.resolve();
            },
        }],
        attrs: {
          placeholder: "请输入审批意见1-100字符",
        }
      }],
    formData: { id },
    okText: props.params.outcome === 'agree' ? '通过' : '驳回',
    visible: true,
    title: '审批',
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (value: any) => {
      let contentObj = {} as any;
      if (props.params.outcome === 'agree') {
        if (type === 'templateCreate') {
          contentObj = {
            cluster: value.cluster,
            rack: value.rack.join(','),
          };
        } else if (type === 'templateIndecrease') {
          contentObj = {
            force: value.force,
          };
        }
      }
      const orderParams = {
        assignee: props.user.getName('domainAccount'),
        checkAuthority: false,
        comment: value.opinion.trim(),
        orderId: id + '',
        outcome: props.params.outcome,
        assigneeAppid: props.app.appInfo()?.id,
        contentObj,
      } as unknown as IApprovalOrder;
      approvalOrder(orderParams).then((res) => {
        let msg = props.params.outcome === 'agree' ? '通过成功' : '驳回成功';
        if (res?.message) {
          msg = res?.description;
        }
        notification.success({ message: msg });
        props.dispatch(actions.setModalId(''));
        props.cb();
      });
    },
  };

  if (props.params.outcome === 'agree') {
    if (type === 'templateCreate') {
      xFormModalConfig.formMap.splice(1, 0, ...formtTemplateCreateMap);
    }
  }

  if (props.params.outcome === 'agree') {
    if (type === 'templateIndecrease') {
      // xFormModalConfig.formMap.splice(1, 0, ...formtTemplateIndecreaseMap);
    }
  }

  return (
    <>
      <XFormWrapper
        visible={true}
        {...xFormModalConfig}
      />
    </>
  )
});
