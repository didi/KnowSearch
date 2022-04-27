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

const AddOrEditProjectModal = (props: { dispatch: any, cb: Function, params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'project',
        label: '项目名',
        attrs: {
          placeholder: '请输入项目名',
        },
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            const reg = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/;
            if (!reg.test(value)) {
              return Promise.reject('请输入正确格式');
            } else {
              return Promise.resolve();
            }
          },
        }],
      }, {
        key: 'principal',
        label: '项目负责人',
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
        key: 'ssh',
        type: FormItemType.input,
        label: '密钥',
        rules: [{
          required: true,
          whitespace: true,
          message: '请输入',
        }],
      }, {
        key: 'QPS',
        type: FormItemType.input,
        label: 'ES查询限流值（QPS）',
        rules: [{
          required: true,
          whitespace: true,
          message: '请输入',
        }],
      }, {
        key: 'description',
        type: FormItemType.textArea,
        label: '项目描述',
        rules: [{
          required: true,
          whitespace: true,
          validator: (rule: any, value: string) => {
            if (value?.trim().length > 0 && value?.trim().length < 100) {
              return Promise.reject('0-100个字符');
            } else {
              return Promise.resolve();
            }
          },
        }],
        attrs: {
          placeholder: '请输入该项目描述，0-100字',
        },
      }, {
        key: 'member',
        type: FormItemType.custom,
        label: '成员列表',
        customFormItem: <MemberTransfer />,
      }
    ] as IFormItem[],
    visible: true,
    title: props.params ? '编辑项目' : '新建项目',
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

  if (!props.params) {
    xFormModalConfig.formMap.splice(2, 1);
  }
  return (
    <>
      <XFormWrapper
        visible={true}
        {...xFormModalConfig}
      />
    </>
  )
};

export default connect(mapStateToProps)(AddOrEditProjectModal);

