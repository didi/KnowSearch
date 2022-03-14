import * as React from 'react';
import 'styles/search-filter.less';
import { getOperationColumns } from './config';
import Url from 'lib/url-parser';
import { DTable } from 'component/dantd/dtable';
import { getUserRecordMultiList } from 'api/cluster-api';
import { IOperaRecordt } from 'typesPath/cluster/cluster-types';
import { connect } from 'react-redux';
import * as actions from "actions";

const mapDispatchToProps = (dispatch: any) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setDrawerId(modalId, params, cb)),
});

const connects: Function = connect;
@connects(null, mapDispatchToProps)
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
    getUserRecordMultiList({moduleId: '1, 13', bizId: this.id}).then((res) => {
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
          columns={getOperationColumns((this.props as any)?.setDrawerId)}
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
