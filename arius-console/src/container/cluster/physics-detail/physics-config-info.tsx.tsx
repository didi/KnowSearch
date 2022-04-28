import * as React from "react";
import "styles/search-filter.less";
import { getConfigInfoColumns } from "./config";
import Url from "lib/url-parser";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { getPhyClusterConfigList } from "api/op-cluster-config-api";
import { isOpenUp } from "constants/common";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setDrawerId(modalId, params)),
  setPhyClusterConfigList: (configList: any) =>
    dispatch(actions.setPhyClusterConfigList(configList)),
});
const connects: Function = connect

@connects(null, mapDispatchToProps)
export class PhysicsConfigInfo extends React.Component<any> {
  public state = {
    searchKey: "",
    configList: [],
    loading: false,
  };

  public clusterId: number;
  public cluster: string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.clusterId = Number(url.search.physicsClusterId);
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
    getPhyClusterConfigList(this.clusterId)
      .then((res) => {
        this.setState({
          configList: res,
        });
        this.props.setPhyClusterConfigList(res);
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
        label: "新增配置",
        className: "ant-btn-primary",
        isOpenUp: isOpenUp,
        clickFunc: () => {
          this.props.setModalId("newConfigModal", {}, this.reloadData);
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
    const { configList, loading } = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(configList)}
          columns={getConfigInfoColumns(
            this.props.setModalId,
            this.reloadData,
            this.props.setDrawerId
          )}
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
