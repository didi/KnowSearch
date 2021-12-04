import * as React from 'react';
import 'styles/search-filter.less';
import { getOperationColumns } from './config';
import Url from 'lib/url-parser';
import { DTable } from 'component/dantd/dtable';
import { getUserRecordList } from 'api/cluster-api';
import { IOperaRecordt } from '@types/cluster/cluster-types';

export class OperatingRecord extends React.Component {
  public state = {
    searchKey: '',
    data: [] as IOperaRecordt[],
    loading: false
  };

  public id: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
  }

  public getData = (origin?: IOperaRecordt[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + '').trim().toLowerCase();
    const data = searchKey ? origin.filter(
      (d) =>
        d.content &&
        d.content.toLowerCase().includes(searchKey as string),
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

    getUserRecordList({moduleId: 1, bizId: this.id}).then((res) => {
      this.setState({
        data: res || []
      })
    }).finally(() => {
      this.setState({
        loading: false
      })
    })
  }

  public handleSubmit = (value) => {
    this.setState({
      searchKey: value
    })
  }

  public render () {
    const {data, loading} = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(data)}
          columns={getOperationColumns()}
          reloadData={this.reloadData}
          tableHeaderSearchInput={{submit: this.handleSubmit}}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return 'no-padding';
  }
}
