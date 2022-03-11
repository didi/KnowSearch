import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { RenderText } from 'container/custom-form';
import { VERSION_MAINFEST_TYPE } from 'constants/status-map';
import { IOpPhysicsClusterDetail } from 'typesPath/cluster/cluster-types';
import { opEditCluster } from 'api/cluster-api';
import { notification } from 'antd';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const EditPhyCluster = (props: { dispatch: any, cb: Function, params: IOpPhysicsClusterDetail }) => {

  const xFormModalConfig = {
    formMap: [
      {
        key: 'type',
        label: '集群类型',
        type: FormItemType.text,
        customFormItem: <RenderText text={VERSION_MAINFEST_TYPE[props.params.type]} />,
      }, {
        key: 'name',
        label: '集群名称',
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.cluster} />,
      }, {
        key: 'project',
        label: '所属项目',
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params?.belongAppNames?.join(',') || '-'} />,
      },
      {
        key: 'desc',
        type: FormItemType.textArea,
        label: '集群描述',
        rules: [{
          validator: (rule: any, value: string) => {
            if (props.params?.desc === value) {
              return Promise.reject('请编辑描述，不可与原本一致。');
            }
            if(!value || value?.trim().length < 100){
              return Promise.resolve();
            } else {
              return Promise.reject('请输入0-100字描述信息');
            }
          }, 
        }],
        attrs: {
          placeholder: '请输入该项目描述，0-100字',
        },
      }
    ] as IFormItem[],
    visible: true,
    title: '编辑集群',
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (result: any) => {
      const req = props.params;
      const { responsible,  desc} = result;
      req.responsible = Array.isArray(responsible) ? responsible.join(',') : responsible;
      req.desc = desc;
      return opEditCluster(req).then(() => {
        notification.success({ message: `编辑成功` });
      }).finally(() => {
        props.cb && props.cb();
        props.dispatch(actions.setModalId(''));
      });
    },
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

export default connect(mapStateToProps)(EditPhyCluster);

