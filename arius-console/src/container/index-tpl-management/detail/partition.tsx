import * as React from "react";
import "styles/search-filter.less";
import { getIndexPartitionColumns } from "./config";
import Url from "lib/url-parser";
import { getIndexPartitionList } from "api/cluster-index-api";
import { DTable } from "component/dantd/dtable";

export class IndexPartition extends React.Component {
  public state = {
    searchKey: "",
    indexList: [],
    loading: false,
  };

  public id: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = Number(url.search.id);
  }

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + "").trim().toLowerCase();
    const data = searchKey ? origin.filter((d) => d.index && d.index.toLowerCase().includes(searchKey as string)) : origin;
    return data;
  };

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    this.setState({
      loading: true,
    });

    getIndexPartitionList(this.id)
      .then((res) => {
        this.setState({
          indexList: (res || []).sort((a, b) => a.id - b.id) || [],
        });
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  public handleSubmit = (value) => {
    this.setState({
      searchKey: value,
    });
  };

  public render() {
    const { indexList, loading } = this.state;
    const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(indexList)}
          columns={getIndexPartitionColumns()}
          reloadData={this.reloadData}
          tableHeaderSearchInput={{ submit: this.handleSubmit }}
          attrs={{
            scroll: {
              y: clientHeight > 500 ? clientHeight - 200 : 300,
            },
          }}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return "no-padding";
  };
}
