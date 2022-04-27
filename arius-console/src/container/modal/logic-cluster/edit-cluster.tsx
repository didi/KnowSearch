import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { notification } from 'antd';
import { StaffSelect } from 'container/staff-select';
import { opEditLogicCluster } from 'api/cluster-api';
import { RenderText } from 'container/custom-form';
import { AppState } from 'store/type';
import { staffRuleProps } from 'constants/table';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
});

const EditClusterModal = (props: { dispatch: any, app: AppState, cb: Function, params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'name',
        label: '集群名称',
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.name} />,
      }, {
        key: 'project',
        label: '所属项目',
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.appName} />,
      }, {
        key: 'responsible',
        label: '责任人',
        // colSpan: 10,
        // rules: [
        //   {
        //     required: true,
        //     ...staffRuleProps,
        //   },
        // ],
        // isCustomStyle: true,
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.responsible} />,
      }, {
        key: 'memo',
        type: FormItemType.textArea,
        label: '集群描述',
        rules: [{
          required: false,
          whitespace: true,
          validator: (rule: any, value: string) => {
            if (!value) {
              return Promise.resolve();
            }
            if (value?.trim().length >= 0 && value?.trim().length < 100) {
              return Promise.resolve();
            } else {
              return Promise.reject('0-100个字符');
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
      req.responsible = Array.isArray(result.responsible) ? result.responsible.join(',') : result.responsible;
      req.memo = result.memo,
        opEditLogicCluster(req).then((res) => {
          notification.success({ message: `编辑成功` });
          props.dispatch(actions.setModalId(''));
        }).finally(() => {
          props.cb && props.cb(); // 重新获取数据列表
        });;
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

export default connect(mapStateToProps)(EditClusterModal);

