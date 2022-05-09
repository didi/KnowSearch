import * as React from "react";
import "styles/search-filter.less";
import { getNodeDivideColumns } from "./config";
import Url from "lib/url-parser";
import { INodeDivide } from "typesPath/index-types";
import { DTable, ITableBtn } from "component/dantd/dtable";
import { getPhyNodeDivideList } from "api/op-cluster-index-api";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import _ from "lodash";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setDrawerId(modalId, params)),
});

const connects: any = connect;

@connects(null, mapDispatchToProps)
export class NodeDivide extends React.Component<{
  setModalId?: any;
  setDrawerId?: any;
}> {
  public state = {
    searchKey: "",
    nodeDivideList: [] as INodeDivide[],
    loading: false,
    sort: "",
    pageSize: 10,
    data: [],
  };

  public cluster: string;
  public clusterId: number;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.cluster = url.search.physicsCluster;
    this.clusterId = Number(url.search.physicsClusterId);
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

  public tableChange = (pagination, __, sort) => {
    const { pageSize } = pagination;
    const res = this.state.data;
    if (pageSize !== this.state.pageSize) {
      this.setState(
        {
          pageSize,
        },
        () => {
          this.setNodeDivideList(res);
        }
      );
    } else {
      if (sort.order === "descend") {
        this.setNodeDivideList(res, sort.order);
      } else if (sort.order === "ascend") {
        this.setNodeDivideList(res, sort.order);
      }
    }
  };

  public setNodeDivideList = (res: INodeDivide[], sortOrder = "ascend") => {
    res = _.cloneDeep(res);
    let regionIdArr = res
      ?.filter((item) => item.regionId)
      .sort((a: any, b: any) => {
        if (sortOrder === "descend") {
          return b.regionId - a.regionId;
        } else {
          return a.regionId - b.regionId;
        }
      });

    let noRegionIdArr = res?.filter((item) => !item.regionId);

    // 根据 pageSize 大小判断合并单元格数，避免出现，分页多合并单元格的情况
    const pageSize = this.state.pageSize;
    let start = 0;
    let end = 0;
    regionIdArr = regionIdArr.map((item, index) => {
      if (index % pageSize === 0) {
        start = end;
        end += pageSize;
      }
      const pageArr = regionIdArr.slice(start, end);
      return {
        rowSpan: pageArr?.filter((i) => item.regionId === i.regionId).length,
        clusterRowSpan: this.getClusterRowSpan(pageArr, item),
        racksRowSpan: this.getRackRowSpan(pageArr, item),
        sortId: item.regionId,
        ...item,
      };
    });

    const maxSortId = Math.pow(2, 60);
    noRegionIdArr = noRegionIdArr.map((item, index) => {
      item.regionId = "_";
      return {
        ...item,
        sortId: maxSortId,
      };
    });
    const dataList = [].concat(regionIdArr).concat(noRegionIdArr);

    for (let i = 0; i < dataList.length; i++) {
      let temp = dataList[i];
      for (let j = i + 1; j < dataList.length; j++) {
        if (temp.ip === dataList[j].ip) {
          if (Array.isArray(temp.role)) {
            temp.role.push(dataList[j].role);
            temp.status.push(dataList[j].status);
          } else {
            temp.role = [temp.role, dataList[j].role];
            temp.status = [temp.status, dataList[j].status];
          }
          dataList.splice(j, 1);
          j--;
        }
      }
    }

    this.setState({
      nodeDivideList: dataList.map((item, index) => ({
        ...item,
        logicDepart: item.cluster,
        index,
      })),
    });
  };

  public reloadData = () => {
    this.setState({
      loading: true,
    });
    getPhyNodeDivideList(this.clusterId)
      .then((res) => {
        this.setState(
          {
            data: res,
          },
          () => {
            const res = this.state.data;
            this.setNodeDivideList(res);
          }
        );
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  public getClusterRowSpan = (dataList: INodeDivide[], item: INodeDivide) => {
    const rowSpanArr = dataList.filter((i) => item.regionId === i.regionId);
    const clusterNameRowSpanArr = rowSpanArr.filter(
      (i) => item.logicClusterName === i.logicClusterName
    );
    let spanArr = 0;
    if (clusterNameRowSpanArr[0]?.ip === item.ip) {
      spanArr = clusterNameRowSpanArr.length;
    }
    return spanArr;
  };

  public getRackRowSpan = (dataList: INodeDivide[], item: INodeDivide) => {
    const rowSpanArr = dataList.filter((i) => item.regionId === i.regionId);
    const clusterNameRowSpanArr = rowSpanArr?.filter(
      (i) => item.logicClusterName === i.logicClusterName
    );
    const rackRowSpanArr = clusterNameRowSpanArr?.filter(
      (i) => item.rack === i.rack
    );
    let spanArr = 0;
    if (rackRowSpanArr[0]?.ip === item.ip) {
      spanArr = rackRowSpanArr.length;
    }
    return spanArr;
  };

  public handleSubmit = (value) => {
    this.setState({
      searchKey: value,
    });
  };

  public getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "新增region",
        className: "ant-btn-primary",
        clickFunc: () =>
          this.props.setModalId(
            "newRegionModal",
            {
              clusterName: this.cluster,
              nodeDivideList: this.state.nodeDivideList,
            },
            this.reloadData
          ),
      },
    ];
  };

  public render() {
    const { nodeDivideList, loading } = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(nodeDivideList)}
          columns={getNodeDivideColumns(
            nodeDivideList,
            this.props.setModalId,
            this.props.setDrawerId,
            this.reloadData,
            this.cluster
          )}
          reloadData={this.reloadData}
          tableHeaderSearchInput={{ submit: this.handleSubmit }}
          getOpBtns={this.getOpBtns}
          attrs={{
            bordered: true,
            onChange: this.tableChange,
          }}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return "no-padding";
  };
}
