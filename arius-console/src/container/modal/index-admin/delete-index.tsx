import { DeleteOutlined } from '@ant-design/icons';
import { Button, Checkbox, Drawer, message, Modal } from 'antd'
import { use } from 'echarts';
import React, { memo, useEffect, useState } from 'react'
import { useSelector, useDispatch, shallowEqual } from 'react-redux';
import './index.less';
import * as actions from "../../../actions";
import { delIndexAdminData } from 'api/index-admin';

export const DeleteIndex = memo((props) => {
  const dispatch = useDispatch();
  const [isDisabled, setIsDisabled] = useState(true);
  const { params, cb }= useSelector(state => ({params: (state as any).modal.params, cb: (state as any).modal.cb }), shallowEqual);

  const { delList, title }  = params;

  const [loading, setIsLoading] = useState(false);

  const del = async () => {
    setIsLoading(true);
    try {
      const res = await delIndexAdminData(delList);
      res ? message.success('删除成功') : message.error('删除失败!');      
    } catch (error) {
      message.error('删除失败!');     
    } finally {
      // 传递参数刷新数据判断是删除操作
      cb(true);
      setIsLoading(false);
      dispatch(actions.setModalId(""));
    }
  }

  return (
    <Modal
      visible={true}
      maskClosable={false}
      onOk={() => {
        setIsDisabled(true);
        dispatch(actions.setModalId(""));
      }}
      onCancel={
        () => {
          setIsDisabled(true);
          dispatch(actions.setModalId(""));
        }
      }
      className={"delete-index-container"}
      footer={[]}
    >
      <div className="ant-modal-confirm-title">
        <DeleteOutlined style={{ color: "red" }} /> 
        <p>{title}</p>
      </div>
      <p>
        <Checkbox 
          onChange={() => {
            setIsDisabled(!isDisabled);
          }}
          style={{ margin: "0 15px" }}
        />
        <span>索引删除后数据无法恢复，请确认影响后继续删除操作</span>
      </p>
      <div className="delete-index-container-button">
        <Button key="cancel" onClick={() => dispatch(actions.setModalId(""))} >
          取消
        </Button>
        <Button
          type="primary"
          key="ok"
          disabled={isDisabled}
          loading={loading}
          onClick={() => {
            del();
          }}
          style={{marginLeft: 8}}
        >
          确认
        </Button>
      </div>
    </Modal>)
});

