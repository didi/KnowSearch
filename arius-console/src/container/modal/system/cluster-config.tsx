import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { notification }  from 'antd';
import { IDeploy } from '@types/cluster/physics-type';
import { newDeploy, updateDeploy } from 'api/cluster-api';

const mapStateToProps = state => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const ClusterConfigModal = connect(mapStateToProps)((props: { dispatch: any, params: IDeploy, cb: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'valueGroup',
        label: '配置组 ',
        attrs: {
          placeholder: '请填写配置组名称',
        },
        rules: [
          { 
            required: true,
            validator: (rule: any, value: string) => {
              let flat_1_50 = (value && value.length > 0 && value.length <= 50);
              if (!value) {
                return Promise.reject('配置组不能为空');
              }
              if(flat_1_50) {
                return Promise.resolve();
              } else {
                return Promise.reject('请输入1-50字符');
              }
            }, 
          },
        ],
      }, 
      {
        key: 'valueName',
        label: '名称',
        attrs: {
          placeholder: '请填写名称',
        },
        rules: [
          { 
            required: true,
            validator: (rule: any, value: string) => {
              let flat_1_50 = (value && value.length > 0 && value.length <= 50);
              if (!value) {
                return Promise.reject('名称不能为空');
              }
              if(flat_1_50) {
                return Promise.resolve();
              } else {
                return Promise.reject('请输入1-50字符');
              }
            }, 
          },
        ],
      },
      {
        key: 'value',
        label: '值',
        type: FormItemType.textArea,
        rules: [{ 
          required: false,
          validator: (rule: any, value: string) => {
            if (!value) {
              return Promise.resolve();
            }
            let flat_0_100 = (value.length >= 0 && value.length <= 100);
            if(flat_0_100) {
              return Promise.resolve();
            } else {
              return Promise.reject('请输入0-100个字符');
            }
          }
        }],
        attrs: {
          placeholder: `请填写值`,
          rows: 4,
        },
      },
      {
        key: 'memo',
        label: '描述',
        type: FormItemType.textArea,
        rules: [
          { 
            required: false,
            validator: (rule: any, value: string) => {
              if (!value) {
                return Promise.resolve();
              } else if (value?.trim().length > 100) {
                return Promise.reject('请输入0-100个字符');
              }
              return Promise.resolve();
            },
          }
        ],
        attrs: {
          placeholder: `请概要描述`,
          rows: 4,
        },
      },
    ] as IFormItem[],
    visible: true,
    title: `${ props.params?.id ? '编辑' : '新增' }配置`,
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (result: any) => {
      if (props.params?.id) {
        result.id = props.params?.id;
        updateDeploy(result).then(() => {
          notification.success({ message: '编辑配置成功' });
          props.dispatch(actions.setModalId(''));
        }).finally(() => {
          props.cb && props.cb();
        });
      } else {
        result.status = 1;
        props.dispatch(actions.setModalId(''));
        newDeploy(result).then(() => {
          notification.success({ message: '新建配置成功' });
        }).finally(() => {
          props.cb && props.cb();
        });
      }
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
});
