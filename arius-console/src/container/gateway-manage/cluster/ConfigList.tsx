import * as React from "react";
import "styles/search-filter.less";
import { getConfigListColumns } from "./config";
import Url from "lib/url-parser";
import { DTable } from "component/dantd/dtable";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { IPlug } from "typesPath/plug-types";
import { getGatewayConfigList } from "api/gateway-manage";

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
  public clusterId: number;
  public componentId: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.cluster = url.search.name;
    this.clusterId = +url.search.id;
    this.componentId = url.search.componentId;
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
    getGatewayConfigList(this.clusterId, {
      groupName: this.state.searchKey,
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
          attrs={this.onChange}
        />
      </div>
    );
  }
}
