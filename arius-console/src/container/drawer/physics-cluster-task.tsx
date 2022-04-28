import * as React from 'react';
import { Button, Drawer, Modal, message } from 'antd';
import { connect } from "react-redux";
import * as actions from 'actions';
import { collectPhysicsClusterNodeConfig, createTomorrowIndex, deleteExpiredIndex } from 'api/cluster-api';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
});

export class PhysicsClusterTaskDrawer extends React.Component<any> {

  public showSuccessMessage = () => {
    message.success('任务触发请求已发送');
  }

  public createTomorrowIndex = () => {
    Modal.confirm({
      title: '确认',
      content: '您确认触发创建索引吗?',
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        createTomorrowIndex(this.props.params?.cluster).then(() => {
          this.showSuccessMessage();
        });
      },
    });
  }

  public deleteExpiredIndex = () => {
    Modal.confirm({
      title: '确认',
      content: '您确认删除过期索引吗?',
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        deleteExpiredIndex(this.props.params?.cluster).then(() => {
          this.showSuccessMessage();
        });
      },
    });
  }

  public collectPhysicsClusterNodeConfig = () => {
    Modal.confirm({
      title: '确认',
      content: '您确认采集节点配置信息吗?',
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        collectPhysicsClusterNodeConfig(this.props.params?.cluster).then(() => {
          this.showSuccessMessage();
        });
      },
    });
  }

  public render() {
    return (
      <>
        <Drawer
          visible={true}
          onClose={() => this.props.dispatch(actions.setDrawerId(''))}
          title={
            <span>
              集群任务
            </span>
          }
          width={500}
        >
          <div className="head-tip">当前集群: {this.props.params?.cluster}</div>
          <div className="mb-24">
            <Button type="primary" onClick={this.createTomorrowIndex}>创建明天索引</Button>
          </div>
          <div className="mb-24">
            <Button type="primary" onClick={this.deleteExpiredIndex}>删除过期索引</Button>
          </div>
          <div className="mb-24">
            <Button type="primary" onClick={this.collectPhysicsClusterNodeConfig}>采集节点配置</Button>
          </div>
        </Drawer>
      </>
    );
  }
}

export default connect(mapStateToProps)(PhysicsClusterTaskDrawer);