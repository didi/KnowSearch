import * as React from 'react';
import { XForm as XFormComponent } from 'component/x-form';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import  { notification, Modal, Spin }  from 'antd';
import { regNonnegativeInteger } from 'constants/reg';
import { getPhysicalTemplateIndexDetail, updateTemplateIndex } from 'api/cluster-index-api';
import { IOpTemplateIndexDetail } from 'typesPath/index-types';

const mapStateToProps = state => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const PhyUpgradeIndex = connect(mapStateToProps)((props: { dispatch: any, cb: Function,  params: number }) => {
  const [data, setData]: any = React.useState([] as IOpTemplateIndexDetail[]);
  const [confirmLoading, setConfirmLoading] = React.useState(false);
  const formRefMap = React.useRef(new Map());

  //改造为多集群
  // const $formRef: any = React.createRef();

  React.useEffect(() => {
    setConfirmLoading(true);
    getPhysicalTemplateIndexDetail(props.params).then((data: IOpTemplateIndexDetail[]) => {
      setData(data)
    }).finally(() => {
      setConfirmLoading(false);
    });
  }, []);

  const xFormModalConfig = {
    formMap: [
      {
        key: 'rack',
        label: '模版rack',
        attrs: {
          placeholder: '请填写模版rack',
        },
        rules: [
          { required: true, message: '请填写模版rack' },
        ],
      },
      {
        key: 'shard',
        label: 'shard个数',
        type: FormItemType.inputNumber,
        attrs: {
          placeholder: '请填写shard个数',
        },
        rules: [
          {
            required: true,
            message: '请填写shard个数（正整数）',
            validator: (rule: any, value: any) => {
              if(new RegExp(regNonnegativeInteger).test(value)) return Promise.resolve();
              return Promise.reject();
            },
          },
        ],
      },
      {
        key: 'version',
        label: '版本号',
        type: FormItemType.inputNumber,
        attrs: {
          placeholder: '请填写版本号',
        },
        rules: [
          {
            required: true,
            message: '请填写版本号（正整数）',
            validator: (rule: any, value: any) => {
              if(new RegExp(regNonnegativeInteger).test(value)) return Promise.resolve();
              return Promise.reject();
            },
          },
        ],
      },
    ] as IFormItem[],
    visible: true,
    title: '升级版本',
    isWaitting: true,
    width: 500,
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
  };

  const handleSubmit = async () => {
    try {
      const params = [];
      for (const [key, value] of formRefMap.current.entries()) {
        const param = await value.current?.validateFields();
        param.physicalId = key;
        param.logicId = '';
        data.forEach(item => {
          if (item.id == key) {
            param.logicId = item.logicId
          }
        })
        params.push(param)
      } 
      updateTemplateIndex(params).then(() => {
        notification.success({ message: `升级模板成功` });
        props.dispatch(actions.setModalId(''));
      }).finally(() => {
        props.dispatch(actions.setModalId(''));
        props.cb && props.cb(); // 重新获取数据列表
      });
    } catch (e) {
      console.log(e);
    }
  //  $formRef.current!.validateFields().then(result => {
  //     result.physicalId = data.id;
  //     updateTemplateIndex(result).then(() => {
  //       notification.success({ message: `提交升级申请成功！` });
  //       props.dispatch(actions.setModalId(''));
  //     }).finally(() => {
  //       props.dispatch(actions.setModalId(''));
  //       props.cb && props.cb(); // 重新获取数据列表
  //     });
  //   })
  }

  return (
    <>
      <Modal
        width={xFormModalConfig.width}
        title={xFormModalConfig.title}
        visible={xFormModalConfig.visible}
        confirmLoading={confirmLoading}
        maskClosable={false}
        onOk={handleSubmit}
        onCancel={xFormModalConfig.onCancel}
        okText={'确定'}
        cancelText={'取消'}
      >
        <Spin spinning={confirmLoading}>
        {
          data && data.length ? 
          data.map((item) => {
            const ref =  React.createRef();
            formRefMap.current.set(item.id, ref)
            return <div key={item.id}>
              <div style={{ marginBottom: 10 }}>
                <span>物理集群：</span> {item.cluster} 
              </div>
              <XFormComponent
                wrappedComponentRef={ref}
                formData={item}
                key={item.id}
                formMap={xFormModalConfig.formMap}
                layout={'vertical'}
              />
            </div>
          })
          :
          '获取详情失败！'
        }
        </Spin>
      </Modal>
    </>
  )
});

