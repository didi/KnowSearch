import * as React from 'react';
import { XFormWrapper } from 'component/x-form-wrapper';
import { connect } from "react-redux";
import * as actions from '../../../actions';
import { FormItemType, IFormItem } from 'component/x-form';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

interface IProps {
  onChange?: (e: any) => any;
  value?: string[];
}


const TransferOfResources = (props: { dispatch: any, cb: Function, params: any }) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: 'principal',
        label: '转让项目名称',
        type: FormItemType.select,
        options: [{
          label: '爸爸1',
          value: 1,
        }],
        rules: [{
          required: true,
          message: '请选择转让项目名称',
        }],
        attrs: {
          mode: 'multiple'
        }
      },
    ] as IFormItem[],
    visible: true,
    title: '转让资源',
    formData: {},
    isWaitting: true,
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
    onSubmit: (value: any) => {
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

export default connect(mapStateToProps)(TransferOfResources);

