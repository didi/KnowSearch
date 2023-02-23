import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from '../../../actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { Cascader } from 'antd';

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

export class CustomCascader extends React.Component<IProps> {
  public state = {
    data: [
      {
        value: 'zhejiang',
        label: 'Zhejiang',
        children: [
          {
            value: 'hangzhou',
            label: 'Hangzhou',
            children: [
              {
                value: 'xihu',
                label: 'West Lake',
              },
            ],
          },
        ],
      },
      {
        value: 'jiangsu',
        label: 'Jiangsu',
        children: [
          {
            value: 'nanjing',
            label: 'Nanjing',
            children: [
              {
                value: 'zhonghuamen',
                label: 'Zhong Hua Men',
              },
            ],
          },
        ],
      },
    ]
  };

  public handleChange = (targetKeys: any) => {
    this.setState({ targetKeys });
    const { onChange } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(targetKeys);
  }

  public render() {
    return (
      <Cascader options={this.state.data} onChange={this.handleChange} placeholder="请选择细分权限" />
    );
  }
}



const ResourcesAssociated = (props: { dispatch: any, cb: Function, params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'type',
        label: '资源类型',
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
          disabled: props.params,
          placeholder: '请选择新增的资源类型',
        }
      }, {
        key: 'name',
        label: '资源名称',
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
          mode: 'multiple',
          disabled: props.params,
          placeholder: '请选择资源所属的项目',
        }
      }, {
        key: 'member',
        type: FormItemType.custom,
        label: '细分权限',
        customFormItem: <CustomCascader />,
      }
    ] as IFormItem[],
    visible: true,
    title: '关联资源',
    formData: props.params || {},
    isWaitting: true,
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

export default connect(mapStateToProps)(ResourcesAssociated);

