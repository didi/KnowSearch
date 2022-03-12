import * as React from "react";
import "styles/search-filter.less";
import { getPlugnListColumns } from "./config";
import Url from "lib/url-parser";
import { IIndex } from "@types/index-types";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { getClusterPlugList } from "api/plug-api";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import urlParser from "lib/url-parser";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});

@connect(null, mapDispatchToProps)
export class PlugnList extends React.Component<any> {
  public state = {
    searchKey: "",
    indexList: [] as IIndex[],
    loading: false,
  };

  public clusterId: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
  }

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + "").trim().toLowerCase();
    const data = searchKey
      ? origin.filter((d) => {
          let flat = false;
          Object.keys(d).forEach((key) => {
            if (typeof key === "string" || typeof key === "number") {
              if (
                (d[key] + "").toLowerCase().includes((searchKey + "") as string)
              ) {
                flat = true;
                return;
              }
            }
          });
          return flat;
        })
      : origin;
    return data;
  };

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    this.setState({
      loading: true,
    });
    const { physicsCluster } = urlParser().search;
    getClusterPlugList(physicsCluster)
      .then((res) => {
        this.setState({
          indexList: res || [],
        });
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  public getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "安装自定义插件",
        className: "ant-btn-primary",
        isOpenUp: true,
        clickFunc: () => {
          this.props.setModalId("customPlugn", {}, this.reloadData);
        },
      },
    ];
  };

  public handleSubmit = (value) => {
    this.setState({
      searchKey: value,
    });
  };

  public render() {
    const { indexList, loading } = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(indexList)}
          columns={getPlugnListColumns(this.reloadData)}
          reloadData={this.reloadData}
          getOpBtns={this.getOpBtns}
          tableHeaderSearchInput={{ submit: this.handleSubmit }}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return "no-padding";
  };
}
