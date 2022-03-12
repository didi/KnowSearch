import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { ExpectDataNodeNu, RenderText, ShowCost } from 'container/custom-form';
import { IWorkOrder } from '@types/params-types';
import { submitWorkOrder } from 'api/common-api';
import { regNonnegativeInteger } from 'constants/reg';
import { ICluster } from '@types/cluster/cluster-types';
import { AppState, UserState } from 'store/type';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ExpandShrinkModal = (props: { dispatch: any, cb: Function, params: ICluster, app: AppState, user: UserState }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'name',
        label: '集群名称',
        type: FormItemType.text,
        customFormItem: <RenderText  text={props.params.name}/>,
      }, {
        key: 'project',
        label: '所属项目',
        type: FormItemType.text,
        customFormItem: <RenderText  text={props.app.appInfo()?.name}/>,
      }, {
        key: 'oldDataNodeNu',
        label: '现有节点数',
        type: FormItemType.text,
        customFormItem: <RenderText  text={props.params?.dataNodesNumber || 0}/>,
      }, {
        key: 'dataNodesNumber',
        label: '期望节点数',
        type: FormItemType.custom,
        customFormItem: <ExpectDataNodeNu min={0} podNumber={props.params?.dataNodesNumber || 0}/>,
        rules: [{
          required: true,
          validator: (rule: any, value: any,) => {
            if (props.params?.dataNodesNumber === value) {
              return Promise.reject('不能与原来一样');
            }
            if (value < 1) {
              return Promise.reject('请输入节点个数，大于等于1的正整数');
            } else {
              return Promise.resolve();
            }
          },
        }],
      }, 
      // {
      //   key: 'clusterCost',
      //   label: '集群成本',
      //   type: FormItemType.text,
      //   customFormItem: <ShowCost />,
      // }, 
      {
        key: 'description',
        label: '申请原因',
        type: FormItemType.textArea,
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            if(!value || value?.trim().length >= 100) {
              return Promise.reject('请输入1-100字申请原因');
            } else {
              return Promise.resolve();
            }
          }, 
        }],
        attrs: {
          placeholder: '请输入1-100字申请原因',
        },
      },
    ] as IFormItem[],
    visible: true,
    title: '集群扩缩容',
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    okText: '提交',
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (result: any) => {
      const params: IWorkOrder = {
        contentObj: {
          logicClusterId: props.params.id,
          logicClusterName: props.params.name,
          // dataNodeSpec: value.dataNodeSpec,
          dataNodeNu: result.dataNodesNumber,
          memo: result.memo,
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description,
        type: 'logicClusterIndecrease',
      };
      return submitWorkOrder(params);
    }
  };

  return (
    <>
      <XFormWrapper
        visible={true}
        {...xFormModalConfig}
      />
    </>
  )
};

export default connect(mapStateToProps)(ExpandShrinkModal);

