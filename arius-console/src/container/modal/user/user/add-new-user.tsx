import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from '../../../../actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { Input } from 'antd';

interface ITaskValue {
  taskName: string;
  sinkType: string;
  sourceType: string;
  taskDesc: string;
  owners: string[];
  quota: number;
}

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

interface IProps {
  attrs?: any;
  placeholder?: string;
  value?: any;
  disabled?: boolean;
  onChange?: (newValue: any) => void;
}

class InptSuffix extends React.Component<IProps> {
  public handleChange = (params: any) => {
    const { onChange } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(params);
  }

  public render() {
    return <>
      <div>
        <Input onChange={this.handleChange} {...this.props} style={{width: 325}}/> 
        <span style={{paddingLeft: 10}}>账号创建后不允许修改</span>
      </div>
    </>;
  }
}

const AddOrEditUserModal = (props: { dispatch: any, cb: Function, params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'user',
        label: '用户账号',
        type: FormItemType.custom,
        customFormItem: <InptSuffix  placeholder='支持英文、大小写不区分、数字、下划线' disabled={props.params ? true : false}/>,      
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            const reg = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/;
            if(!reg.test(value)){
              return Promise.reject('请输入正确账号件格式');
            } else {
              return Promise.resolve();
            }
          }, 
        }],
      }, {
        key: 'userName',
        label: '用户实名',
        rules: [{ required: false, whitespace: true, message: '格式有误' }],
        attrs: {
          placeholder: '请输入用户名',
        },
      }, {
        key: 'mailbox',
        type: FormItemType.input,
        label: '邮箱',
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            const reg = /^[\w.\-]+@(?:[a-z0-9]+(?:-[a-z0-9]+)*\.)+[a-z]{2,3}$/;
            if(!reg.test(value)){
              return Promise.reject('请输入完整的邮件格式');
            } else {
              return Promise.resolve();
            }
          }, 
        }],
        attrs: {
          placeholder: '请输入邮件地址',
        },
      }, {
        key: 'phone',
        type: FormItemType.input,
        label: '电话',
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            const reg = /^[1][3-9][0-9]{9}$/;
            if(!reg.test(value)){
              return Promise.reject('请输入正确电话号码');
            } else {
              return Promise.resolve();
            }
          }, 
        }],
        attrs: {
          placeholder: '请输入联系方式',
        },
      }, {
        key: 'role',
        label: '角色',
        type: FormItemType.select,
        options: [{
          label: '爸爸1',
          value: 1,
        }],
        rules: [{
          required: true,
          message: '请选择',
        }],
        attrs: {
          mode: 'multiple'
        }
      }, {
        key: 'project',
        label: '所属项目',
        type: FormItemType.select,
        options: [{
          label: '爸爸1',
          value: 1,
        }],
        rules: [{
          required: false,
          message: '请选择',
        }],
        attrs: {
          mode: 'multiple'
        }
      },
    ] as IFormItem[],
    visible: true,
    title: props.params ? '编辑用户' : '新增用户',
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (value: ITaskValue) => {
      props.dispatch(actions.setModalId(''));
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

export default connect(mapStateToProps)(AddOrEditUserModal);

