import * as React from "react";
import "styles/search-filter.less";
import { getPluginListColumns } from "./config";
import Url from "lib/url-parser";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { getOpClusterPlugList } from "api/plug-api";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { IPlug } from "typesPath/plug-types";
import { isOpenUp } from "constants/common";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});
const connects: Function = connect;

@connects(null, mapDispatchToProps)
export class PlugnList extends React.Component<any> {
  public state = {
    searchKey: "",
    plugnList: [] as IPlug[],
    loading: false,
  };

  public cluster: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.cluster = url.search.physicsCluster;
  }

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + "").trim().toLowerCase();
    const data = searchKey
      ? origin.filter((d) => {
          let flat = false;
          Object.keys(d).forEach((key) => {
            if (typeof key === "string" || typeof key === "number") {
              if ((d[key] + "").toLowerCase().includes((searchKey + "") as string)) {
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
    getOpClusterPlugList(this.cluster)
      .then((res) => {
        this.setState({
          plugnList: res,
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
        label: "上传自定义插件",
        className: "ant-btn-primary",
        isOpenUp: isOpenUp,
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
    const { plugnList, loading } = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(plugnList)}
          columns={getPluginListColumns(this.reloadData, this.props.setModalId)}
          tableHeaderSearchInput={{ submit: this.handleSubmit }}
          reloadData={this.reloadData}
          getOpBtns={this.getOpBtns}
        />
      </>
    );
  }
}
