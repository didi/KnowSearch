import * as React from 'react';
import 'styles/search-filter.less';
import { getIndexListColumns } from './config';
import Url from 'lib/url-parser';
import { getOpIndexList } from 'api/cluster-index-api';
import { IIndex } from '@types/index-types';
import { DTable } from 'component/dantd/dtable';
import { getPhysicalTemplateList } from 'api/op-cluster-index-api';

export class IndexList extends React.Component {
  public state = {
    searchKey: '',
    indexList: [] as IIndex[],
    loading: false
  };

  public cluster: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.cluster = url.search.physicsCluster;
  }

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + '').trim().toLowerCase();
    const data = searchKey ? origin.filter(
      (d) => {
        let flat = false;
        Object.keys(d).forEach((key) => {
          if (typeof(key) === 'string' || typeof(key) === 'number') {
            if ((d[key] + '').toLowerCase().includes((searchKey + '') as string)) {
              flat = true;
              return;
            }
          }
        });
        return flat;
      }
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
    getPhysicalTemplateList({cluster: this.cluster}).then((res) => {
      this.setState({
        indexList: res
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
    const {indexList, loading} = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(indexList)}
          columns={getIndexListColumns()}
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
