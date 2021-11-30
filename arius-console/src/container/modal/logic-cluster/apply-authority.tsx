import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { StaffSelect } from 'container/staff-select';
import { RenderText } from 'container/custom-form';
import { IWorkOrder } from '@types/params-types';
import { submitWorkOrder } from 'api/common-api';
import { nounAuthority } from 'container/tooltip';
import { AppState, UserState } from 'store/type';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ApplyAauthorityModal = (props: { dispatch: any, cb: Function, app: AppState, params: any, user: UserState }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'name',
        label: '集群名称',
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.name} />,
      }, {
        key: 'project',
        label: '申请者所在项目',
        type: FormItemType.text,
        customFormItem: <RenderText text={props.app.appInfo()?.name} />,
      }, {
        key: 'powerType',
        label: <span>{nounAuthority} 集群状态: </span>,
        type: FormItemType.text,
        customFormItem: <RenderText text={'访问'} />,
      }, {
        key: 'description',
        type: FormItemType.textArea,
        label: '申请原因',
        rules: [{
          required: true,
          validator: (rule: any, value: string) => {
            if (!value || value?.trim().length > 100) {
              return Promise.reject('请输入1-100字申请原因');
            } else {
              return Promise.resolve();
            }
          },
        }],
        attrs: {
          placeholder: '请输入1-100字申请原因',
        },
      }
    ] as IFormItem[],
    visible: true,
    title: '申请权限',
    formData: props.params || {},
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
          authCode: 2,
          memo: result.description,
        },
        submitorAppid: props.app.appInfo()?.id,
        submitor: props.user.getName('domainAccount'),
        description: result.description,
        type: 'logicClusterAuth',
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

export default connect(mapStateToProps)(ApplyAauthorityModal);

