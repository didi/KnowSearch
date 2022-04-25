import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from '../../../../actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { Transfer } from 'antd';

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
  onChange?: (e: any) => any;
  value?: string[];
}

export class MemberTransfer extends React.Component<IProps> {
  public state = {
    mockData: [] as any,
    targetKeys: [] as any,
    data: [
    {
      key: '1',
      disabled: false,
      title: 'yoyo',
      description: 'ct0',
    },
    {
      key: '2',
      disabled: false,
      title: 'yoyo1',
      description: 'ct2',
    }
  ]
  };

  public filterOption = (inputValue: any, option: any) => option.description.indexOf(inputValue) > -1;

  public handleChange = (targetKeys: any) => {
    this.setState({ targetKeys });
    const { onChange } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(targetKeys);
  }

  public render() {
    return (
      <Transfer
        dataSource={this.state.data}
        showSearch={true}
        filterOption={this.filterOption}
        targetKeys={this.state.targetKeys}
        onChange={this.handleChange}
        render={item => item.title}
      />
    );
  }
}



const AddOrEditRole = (props: { dispatch: any, cb: Function, params: any}) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'role',
        label: '角色名',
        attrs: {
          placeholder: '请输入角色名',
        },
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            const reg = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/;
            if(!reg.test(value)){
              return Promise.reject('请输入正确格式');
            } else {
              return Promise.resolve();
            }
          }, 
        }],
      }, {
        key: 'description',
        type: FormItemType.textArea,
        label: '角色描述',
        rules: [{
          whitespace: true,
          validator: (rule: any, value: string) => {
            if(value?.trim().length > 0 && value?.trim().length < 50){
              return Promise.reject('0-50个字符');
            } else {
              return Promise.resolve();
            }
          }, 
        }],
        attrs: {
          placeholder: '请输入该角色描述，0-50字',
          rows: 2,
        },
      }, {
        key: 'member',
        type: FormItemType.custom,
        label: '权限点',
        customFormItem: <MemberTransfer />,
      }
    ] as IFormItem[],
    visible: true,
    title: props.params ? '编辑角色' : '新增角色',
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

export default connect(mapStateToProps)(AddOrEditRole);

