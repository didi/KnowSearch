import * as React from "react";
import "styles/search-filter.less";
import { getLogicNodeColumns } from "./config";
import Url from "lib/url-parser";
import { getLogicClusterNodeList } from "api/cluster-node-api";
import { DTable, ITableBtn } from "component/dantd/dtable";
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
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});
const connects: Function = connect

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
    nodeList: [] as INode[],
    loading: false,
    pageSize: 10,
    data: [],
  };

  public clusterId: number;
  public type: number;

  public getData = (origin?: any[]) => {
    let { searchKey } = this.state;
    searchKey = (searchKey + "").trim().toLowerCase();
    const data = searchKey
      ? origin.filter((d) => {
          let flat = false;
          Object.keys(d).forEach((key) => {
            if (key === "ip") {
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

  public tableChange(pagination) {
    const { pageSize } = pagination;
    this.setState(
      {
        pageSize,
      },
      () => {
        const res = this.state.data;
        this.setNodeList(res);
      }
    );
  }

  public setNodeList = (res) => {
    res = _.cloneDeep(res);
    let regionIdArr = res
      ?.filter((item) => item.regionId)
      .sort((a, b) => a.regionId - b.regionId);
    let noRegionIdArr = res?.filter((item) => !item.regionId);

    // 根据 pageSize 大小判断合并单元格数，避免出现，分页多合并单元格的情况
    const pageSize = this.state.pageSize;
    let start = 0;
    let end = 0;
    regionIdArr = regionIdArr.map((item, index) => {
      // 当前 page 下的显示的元素
      if (index % pageSize === 0) {
        start = end;
        end += pageSize;
      }
      const pageArr = regionIdArr.slice(start, end);
      return {
        rowSpan: pageArr?.filter((i) => item.regionId === i.regionId).length,
        clusterRowSpan: this.getClusterRowSpan(pageArr, item),
        racksRowSpan: this.getRackRowSpan(pageArr, item),
        ...item,
      };
    });

    noRegionIdArr = noRegionIdArr.map((item, index) => {
      item.regionId = "_";
      return {
        ...item,
      };
    });

    const dataList = [].concat(regionIdArr).concat(noRegionIdArr);

    // 对相同 ip 的元素进行和并
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
      nodeList: dataList.map((item, index) => ({ ...item, index })) || [],
    });
  };

  public reloadData = () => {
    this.setState({
      loading: true,
    });

    getLogicClusterNodeList(this.clusterId)
      .then((res) => {
        this.setState(
          {
            data: res,
          },
          () => {
            const res = this.state.data;
            this.setNodeList(res);
          }
        );
      })
      .finally(() => {
        this.setState({
          loading: false,
        });
      });
  };

  public getClusterRowSpan = (dataList: INode[], item: INode) => {
    const rowSpanArr = dataList.filter((i) => item.regionId === i.regionId);
    const clusterNameRowSpanArr = rowSpanArr.filter(
      (i) => item.cluster === i.cluster
    );
    let spanArr = 0;
    if (clusterNameRowSpanArr[0]?.ip === item.ip) {
      spanArr = clusterNameRowSpanArr.length;
    }
    return spanArr;
  };

  public getRackRowSpan = (dataList: INode[], item: INode) => {
    const rowSpanArr = dataList.filter((i) => item.regionId === i.regionId);
    const clusterNameRowSpanArr = rowSpanArr?.filter(
      (i) => item.cluster === i.cluster
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

  public getOpBtns = (): ITableBtn[] => {
    return [
      {
        label: "关联region",
        className: "ant-btn-primary",
        clickFunc: () => {
          this.props.setModalId(
            "relationRegion",
            { id: this.clusterId, type: this.type, data: this.getData(this.state.nodeList) },
            this.reloadData
          );
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
    const { nodeList, loading } = this.state;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={this.getData(nodeList)}
          columns={getLogicNodeColumns(
            nodeList,
            this.reloadData,
            this.props.logicBaseInfo?.permissions
          )}
          reloadData={this.reloadData}
          getOpBtns={this.getOpBtns}
          tableHeaderSearchInput={{
            submit: this.handleSubmit,
            placeholder: "请输入节点IP",
          }}
          attrs={{ bordered: true, onChange: this.tableChange.bind(this) }}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return "no-padding";
  };
}
