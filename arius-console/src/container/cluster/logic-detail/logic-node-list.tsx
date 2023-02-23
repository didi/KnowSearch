import * as React from "react";
import "styles/search-filter.less";
import { getLogicNodeColumns } from "./config";
import Url from "lib/url-parser";
import { getLogicClusterNodeList } from "api/cluster-node-api";
import { DTable } from "component/dantd/dtable";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import _ from "lodash";

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
  regionId: number;
  cluster: string;
  ip: string;
  rack: string;
}

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});
const connects: Function = connect;

@connects(null, mapDispatchToProps)
export class LogicNodeList extends React.Component<any> {
  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.clusterId);
    this.type = Number(url.search.type);
  }

  public state = {
    searchKey: "",
    loading: false,
    pageSize: 10,
    data: [],
  };

  public clusterId: number;
  public type: number;

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = searchKey.trim().toLowerCase();
    let data = searchKey
      ? origin.filter((d) => {
          if (d.nodeSet.toLowerCase().includes(searchKey)) {
            return true;
          }
          return false;
        })
      : origin;
    data = data.sort((a, b) => b.role - a.role);

    return data;
  };

  public componentDidMount() {
    this.reloadData();
  }

  public tableChange(pagination) {
    const { pageSize } = pagination;
    this.setState({ pageSize });
  }

  public reloadData = () => {
    this.setState({
      loading: true,
    });

    getLogicClusterNodeList(this.clusterId)
      .then((res) => {
        this.setState({ data: res });
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
    const { data, loading } = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(data)}
          columns={getLogicNodeColumns(data, this.reloadData, this.props.logicBaseInfo?.permissions)}
          tableHeaderSearchInput={{
            submit: this.handleSubmit,
            placeholder: "请输入实例名称",
          }}
          attrs={{ bordered: true, onChange: this.tableChange.bind(this) }}
          reloadData={this.reloadData}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return "no-padding";
  };
}
