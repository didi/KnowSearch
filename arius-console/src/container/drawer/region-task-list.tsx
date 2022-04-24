import { Drawer, Table } from 'antd';
import React from 'react';
import { connect } from "react-redux";
import * as actions from 'actions';
import { pagination } from 'constants/table';
import { DTable } from 'component/dantd/dtable';
import { getClusterRegionTaskList } from 'api/op-cluster-region-api';
import { getPhysicsRegionTaskItemColumns } from './config';

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
});

class RegionTaskList extends React.Component<any> {
  public state = {
    loading: false,
    searchKey: '',
    cluterRegionTaskList: [],
    
  }

  public componentDidMount() {
    this.reloadData();
  }

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + '').trim().toLowerCase();
    const data = searchKey ? origin.filter(
      (d) =>
        d.clusterName &&
        d.clusterName.toLowerCase().includes(searchKey as string),
    ) : origin;
    return data;
  }

  
  public reloadData = () => {
    this.setState({
      loading: true
    })
    getClusterRegionTaskList(this.props.params.regionId).then((res) => {
      this.setState({
        nodeDivideList: res
      })
    }).finally(() => {
      this.setState({
        loading: false
      })
    })
  }

  public render() {
    const { dispatch } = this.props

    return (
      <Drawer
        title={'任务列表'}
        visible={true}
        onClose={() => dispatch(actions.setDrawerId(''))}
        width={1200}
        maskClosable={true}
      >
        <DTable
          loading={this.state.loading}
          rowKey="id"
          dataSource={this.getData(this.state.cluterRegionTaskList)}
          columns={getPhysicsRegionTaskItemColumns()}
          reloadData={this.reloadData}
        />
      </Drawer>
    );
  }
}
export default connect(mapStateToProps)(RegionTaskList);
