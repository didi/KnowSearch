import * as React from "react";
import { getPluginListColumns } from "./config";
import Url from "lib/url-parser";
import { DTable } from "component/dantd/dtable";
import { getPhyClusterPlugList } from "api/plug-api";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { IPlug } from "typesPath/plug-types";
import "styles/search-filter.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});
const connects: Function = connect;

@connects(null, mapDispatchToProps)
export class PluginList extends React.Component<any> {
  public state = {
    searchKey: "",
    pluginList: [] as IPlug[],
    loading: false,
  };

  public cluster: string;
  public id: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.cluster = url.search.physicsCluster;
    this.id = url.search.physicsClusterId;
  }

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + "").trim().toLowerCase();
    const data = searchKey ? origin.filter((d) => d.name?.toLowerCase().includes((searchKey + "") as string)) : origin;
    return data;
  };

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    this.setState({
      loading: true,
    });
    getPhyClusterPlugList(this.id)
      .then((res) => {
        this.setState({
          pluginList: res.map((item, index) => ({
            ...item,
            index,
          })),
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
    const { pluginList, loading } = this.state;

    return (
      <div style={{ padding: 20 }}>
        <DTable
          loading={loading}
          rowKey="index"
          dataSource={this.getData(pluginList)}
          columns={getPluginListColumns(this.reloadData, this.props.setModalId, this.props.setDrawerId, this.props.history)}
          tableHeaderSearchInput={{ submit: this.handleSubmit, placeholder: "请输入插件名称" }}
          reloadData={this.reloadData}
        />
      </div>
    );
  }
}
