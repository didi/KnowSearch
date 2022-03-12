import * as React from 'react';
import { Table, Modal, Tooltip, message, Checkbox, notification } from 'antd';
import { QuestionCircleTwoTone } from '@ant-design/icons';
import { deleteIndexInfo, getIndexDeleteInfo } from 'api/cluster-index-api';
import { connect } from "react-redux";
import * as actions from 'actions';
import { IUNSpecificInfo } from '@types/base-types';

const mapStateToProps = state => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const connects: any = connect;

@connects(mapStateToProps)
export class ClearClusterIndex extends React.Component<any> {
  state = {
    relatedAppList: [],
    loading: false,
    checkBtnStatus: false,
  }

  public componentDidMount() {
    this.setState({
      loading: true
    })
    getIndexDeleteInfo(this.props.params).then((value: IUNSpecificInfo) => {
      this.setState({
        relatedAppList: value?.accessApps || [],
      })
    }).finally(() => {
      this.setState({
        loading: false
      });
    });
  }

  public handleCancel = () => {
    this.setState({
      relatedAppList: [],
    })
    this.props.dispatch(actions.setModalId(''));
  }

  public getColumns = () => {
    return [{
      title: '应用名称',
      dataIndex: 'name',
      key: 'name',
    }, {
      title: 'id',
      dataIndex: 'id',
      key: 'id',
    }];
  }

  public handleSubmit = () => {
    const connectionList = this.state.relatedAppList;
    if (connectionList && connectionList.length) {
      return message.warning('存在连接信息，无法申请下线！');
    }

    deleteIndexInfo(this.props.params).then(() => {
      notification.success({ message: `下线模板${this.props.params}成功` });
      this.props.cb();
      this.props.dispatch(actions.setModalId(''));
      // indexStore.getIndexList();
    })
  }

  public render() {
    return (
      <>
        <Modal
          wrapClassName="offline-index"
          visible={true}
          title={
            <span>
              索引下线
              <Tooltip placement="right" title={'如若有及联信息，则表示资源正处于使用中，请确认后再操作。'} >
                <QuestionCircleTwoTone />
              </Tooltip>
            </span>
          }
          maskClosable={false}
          onCancel={this.handleCancel}
          onOk={this.handleSubmit}
          okText="下线"
          cancelText="取消"
          okButtonProps={{ disabled: !this.state.checkBtnStatus }}
          width={700}
        >
          <div className="text">您的索引最近7天有如下AppID在查询：</div>
          <Table
            rowKey="id"
            loading={this.state.loading}
            scroll={{ x: 450, y: 260 }}
            dataSource={this.state.relatedAppList}
            columns={this.getColumns()}
            pagination={false}
            bordered={true}
          />
          <div className="text red">
            <Checkbox checked={this.state.checkBtnStatus} onChange={(e: any) => {this.setState({checkBtnStatus: e.target.checked})}} />
            <span className="ml-5">索引下线后数据无法恢复，请确认影响后继续</span>
          </div>
        </Modal>
      </>
    );
  }
}
