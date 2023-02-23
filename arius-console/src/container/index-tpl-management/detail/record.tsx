import * as React from "react";
import "styles/search-filter.less";
import { getOperationColumns } from "./config";
import Url from "lib/url-parser";
import { DTable } from "component/dantd/dtable";
import { getUserRecordList } from "api/cluster-api";
import { IOperaRecordt } from "typesPath/cluster/cluster-types";
import { connect } from "react-redux";
import { initPaginationProps } from "constants/table";
import * as actions from "actions";

const mapDispatchToProps = (dispatch: any) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const connects: Function = connect;
@connects(null, mapDispatchToProps)
export class OperatingRecord extends React.Component<any> {
  public state = {
    tableData: [] as IOperaRecordt[],
    loading: false,
    queryObject: {
      content: "",
      page: 1,
      size: 10,
    },
    paginationProps: initPaginationProps(),
  };

  public id: number | string;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.id = props?.recordType === "bizId" ? Number(url.search.id) : url.search.index;
  }

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    this.setState({
      loading: true,
    });
    const { queryObject, paginationProps } = this.state;
    getUserRecordList({ bizId: this.id, ...queryObject })
      .then((res) => {
        const { pageNo = 1, pageSize = 10, total = 0 } = res?.pagination;
        this.setState({
          tableData: res?.bizData || [],
          paginationProps: {
            ...paginationProps,
            total: total,
            current: pageNo,
            pageSize: pageSize,
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
    value = value?.trim();
    if (value === this.state.queryObject.content) return;
    this.setState(
      {
        queryObject: {
          ...this.state.queryObject,
          page: 1,
          content: value,
        },
      },
      this.reloadData
    );
  };

  public handleChange = (pagination, filters, sorter) => {
    // 条件过滤请求在这里处理
    const sorterObject: { [key: string]: any } = {};
    // 排序
    if (sorter.field && sorter.order) {
      sorterObject.sortTerm = sorter.field;
      sorterObject.orderByDesc = sorter.order === "ascend" ? false : true;
    }
    if (!sorter.order) {
      delete sorterObject.sortTerm;
      delete sorterObject.orderByDesc;
    }
    this.setState(
      {
        queryObject: {
          ...sorterObject,
          content: this.state.queryObject.content,
          page: pagination.current,
          size: pagination.pageSize,
        },
      },
      this.reloadData
    );
  };

  public render() {
    const { tableData, loading, paginationProps } = this.state;
    const clientHeight = document.querySelector("#d1-layout-main")?.clientHeight;
    return (
      <>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={tableData}
          columns={getOperationColumns((this.props as any)?.setDrawerId)}
          reloadData={this.reloadData}
          tableHeaderSearchInput={{ submit: this.handleSubmit, placeholder: "请输入操作内容" }}
          attrs={{
            onChange: this.handleChange,
            scroll: {
              y: clientHeight > 500 ? clientHeight - 200 : 300,
            },
          }}
          paginationProps={paginationProps}
        />
      </>
    );
  }

  public defineTableWrapperClassNames = () => {
    return "no-padding";
  };
}
