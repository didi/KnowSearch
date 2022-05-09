import * as React from 'react';
import 'styles/search-filter.less';
import { getLogicNodeColumns } from './config';
import Url from 'lib/url-parser';
import { getLogicClusterNodeList } from 'api/cluster-node-api';
import { DTable, ITableBtn } from 'component/dantd/dtable';

export interface INode {
  createTime?: string;
  id: number;
  creator: string;
  desc: string;
  type?: number;
  updateTime?: string;
  md5: string;
  name: string;
  pdefault: string;
  s3url: string;
  url: string;
  version?: string;
  deleteFlag: boolean;
}

export class LogicNodeList extends React.Component {
  public state = {
    searchKey: '',
    nodeList: [] as INode[],
    loading: false
  };

  public clusterId: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
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

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    this.setState({
      loading: true
    })
    getLogicClusterNodeList( this.clusterId).then((res) => {
      this.setState({
        nodeList: res || []
      })
    }).finally(() => {
      this.setState({
        loading: false
      })
    })
  }

  public getOpBtns = (): ITableBtn[] => {
    return [{
      label: '关联region',
      className: 'ant-btn-primary',
      clickFunc: () => {},
    }];
  }

  public handleSubmit = (value) => {
    // 
  }

  public render() {
    const {nodeList, loading} = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(nodeList)}
          columns={getLogicNodeColumns()}
          reloadData={this.reloadData}
          getOpBtns={this.getOpBtns}
          tableHeaderSearchInput={{submit: this.handleSubmit}}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return 'no-padding';
  }
}
