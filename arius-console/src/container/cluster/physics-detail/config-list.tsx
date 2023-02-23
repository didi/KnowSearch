import * as React from "react";
import "styles/search-filter.less";
import { getConfigListColumns } from "./config";
import Url from "lib/url-parser";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { IPlug } from "typesPath/plug-types";
import { getPhysicsClusterConfigList } from "api/cluster-api";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});
const connects: Function = connect;

@connects(null, mapDispatchToProps)
export class ConfigList extends React.Component<any> {
  public state = {
    searchKey: undefined,
    configList: [] as IPlug[],
    pagination: {
      current: 1,
      pageSize: 10,
      total: 0,
    },
    loading: false,
  };

  public cluster: string;
  public clusterId: string;
  public componentId: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.cluster = props.type === "phycluster" ? url.search.physicsCluster : url.search.name;
    this.clusterId = props.type === "phycluster" ? url.search.physicsClusterId : url.search.name;
    this.componentId = props.type === "phycluster" ? url.search.componentId : undefined;
  }

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = (params?: any) => {
    const page = params?.page || this.state.pagination.current;
    const size = params?.size || this.state.pagination.pageSize;
    this.setState({
      loading: true,
    });
    getPhysicsClusterConfigList(+this.clusterId, {
      componentId: this.componentId,
      configName: this.state.searchKey,
      page,
      size,
    })
      .then((res) => {
        this.setState({
          configList: res?.bizData || [],
          pagination: {
            current: res.pagination.pageNo,
            pageSize: res.pagination.pageSize,
            total: res.pagination.total,
          },
        });
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  public handleSubmit = (value) => {
    this.setState(
      {
        searchKey: value,
      },
      () => this.reloadData()
    );
  };
  public onChange = (pagination) => {
    this.reloadData({ page: pagination.current, size: pagination.pageSize });
  };

  public render() {
    const { configList, loading } = this.state;
    return (
      <div style={{ padding: 20 }}>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={configList}
          columns={getConfigListColumns({
            reloadData: this.reloadData,
            setModalId: this.props.setModalId,
            setDrawerId: this.props.setDrawerId,
            clusterId: this.clusterId,
            componentId: this.componentId,
            history: this.props.history,
          })}
          tableHeaderSearchInput={{ submit: this.handleSubmit, placeholder: "请输入配置名称" }}
          reloadData={this.reloadData}
          attrs={{ onChange: this.onChange }}
          paginationProps={{
            total: this.state.pagination.total,
            pageSizeOptions: ["10", "20", "50", "100", "200", "500"],
            showTotal: (total) => `共 ${total} 条`,
            current: this.state.pagination.current,
          }}
        />
      </div>
    );
  }
}
