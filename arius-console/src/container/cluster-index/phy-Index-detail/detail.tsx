import * as React from "react";
import { Button, PageHeader, Spin } from 'antd';
import "styles/detail.less";
import { DESC_LIST } from "./config";
import Url from "lib/url-parser";
import { InfoItem } from "component/info-item";
import { Dispatch } from "redux";
import * as actions from "actions";
import { connect } from "react-redux";
import { getIndexBaseInfo } from "api/cluster-index-api";
import { IBaseButton } from "container/cluster/logic-detail/detail";
import { BaseInfo } from "./base-info";
import { IIndex } from "typesPath/index-types";


const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});
const connects: Function = connect

@connects(null, mapDispatchToProps)
export class PhyIndexDetail extends React.Component<any> {
  public data: IIndex;

  constructor(props: any) {
    super(props);
    const url = Url();
    this.data = JSON.parse(decodeURI(url.search.data) || 'null');
  }

  public componentDidMount() {
    this.reloadData();
  }

  public reloadData = () => {
    // 
  };

  public getOpBtns = (): IBaseButton[] => {
    return [];
  };

  public renderPageHeader() {
    const indexBaseInfo = this.data;
    return (
      <PageHeader
        className="detail-header"
        backIcon={false}
        title={indexBaseInfo.name || ""}
        extra={this.getOpBtns().map((item, index) => (
          <Button
            type={item.type}
            {...item.attr}
            key={index}
            onClick={item.clickFunc}
          >
            {item.label}
          </Button>
        ))}
      >
        {DESC_LIST.map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={
              row.render
                ? row.render(indexBaseInfo?.[row.key])
                : `${indexBaseInfo?.[row.key] || ""}`
            }
            width={250}
          />
        ))}
      </PageHeader>
    );
  }

  public render() {
    return (
      <Spin spinning={false}>
        {this.renderPageHeader()}
        <div className="detail-wrapper">
          <BaseInfo data={this.data} />
        </div>
      </Spin>
    );
  }
}
