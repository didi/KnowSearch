import * as React from 'react';
import { XForm as XFormComponent } from 'component/x-form';
import { connect } from "react-redux";
import * as actions from 'actions';
import { FormItemType, IFormItem } from 'component/x-form';
import { notification, Modal, Spin } from 'antd';
import { getPhysicalTemplateIndexDetail, editTemplateIndex } from 'api/cluster-index-api';
import { IOpTemplateIndexDetail } from 'typesPath/index-types';

const mapStateToProps = state => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const PhyModifyIndex = connect(mapStateToProps)((props: { dispatch: any, cb: Function,  params: number }) => {
  const [data, setData] = React.useState([] as IOpTemplateIndexDetail[]);
  const [confirmLoading, setConfirmLoading] = React.useState(false);
  const formRefMap = React.useRef(new Map());

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
      // {
      //   key: 'name',
      //   label: '模版名',
      //   attrs: {
      //     placeholder: '请填写模版名',
      //     disabled: true,
      //   },
      //   rules: [
      //     { required: true, message: '请填写模版名' },
      //   ],
      // },
      // {
      //   key: 'cluster',
      //   label: '集群',
      //   attrs: {
      //     placeholder: '请填写集群',
      //     disabled: true,
      //   },
      //   rules: [
      //     { required: true, message: '请填写集群' },
      //   ],
      // },
      // {
      //   key: 'role',
      //   label: '角色',
      //   type: FormItemType.select,
      //   options: [{
      //     label: 'master',
      //     value: 1,
      //   }, {
      //     label: 'slave',
      //     value: 2,
      //   }],
      //   attrs: {
      //     placeholder: '请选择',
      //   },
      //   rules: [
      //     { required: true, message: '请选择' },
      //   ],
      // },
      {
        key: 'rack',
        label: 'rack',
        attrs: {
          placeholder: '请填写rack 多个之间用逗号分隔',
        },
        rules: [
          { required: true, message: '请填写rack' },
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
            message: '请填写shard个数',
            validator: (rule: any, value: number) => {
              if(typeof (value) === 'number') return Promise.resolve();
              return Promise.reject();
            },
          },
        ],
      },
      // {
      //   key: 'config',
      //   label: '配置',
      //   type: FormItemType.textArea,
      //   rules: [{
      //     required: false,
      //     message: '请输json格式字符',
      //   }],
      //   attrs: {
      //     placeholder: `请输入配置`,
      //     rows: 4,
      //   },
      // },
    ] as IFormItem[],
    visible: true,
    title: '配置',
    isWaitting: true,
    width: 600,
    onCancel: () => {
      props.dispatch(actions.setModalId(''));
    },
  };

  const handleSubmit = async () => {
    try {
      for (const [key, value] of formRefMap.current.entries()) {
        const param = await value.current?.validateFields();
        data[key].role = param.role;
        data[key].rack = param.rack;
        data[key].shard = param.shard;
        data[key].config = param.config;
      } 
      editTemplateIndex(data).then(() => {
        notification.success({ message: `配置成功！` });
        props.cb && props.cb(); // 重新获取数据列表
      }).finally(() => {
        props.dispatch(actions.setModalId(''));
      });
    } catch (e) {
      console.log(e);
    }
    // $formRef.current!.validateFields().then(result => {
    //   data.role = result.role;
    //   data.rack = result.rack;
    //   data.shard = result.shard;
    //   data.config = result.config;
    //   editTemplateIndex(data).then(() => {
    //     notification.success({ message: `编辑成功！` });
    //     props.cb && props.cb(); // 重新获取数据列表
    //   }).finally(() => {
    //     props.dispatch(actions.setModalId(''));
    //   });
    // })
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
          data.map((item, index) => {
            const ref =  React.createRef();
            formRefMap.current.set(index, ref)
            return <div key={item.id}>
              {index ? null : 
               <div style={{ marginBottom: 10 }}>
                <span>模板名称：</span> {item.name} 
              </div>
              }
              <div style={{ marginBottom: 10 }}>
                <span>所属集群：</span> {item.cluster} 
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

