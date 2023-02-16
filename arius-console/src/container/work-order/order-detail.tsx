import * as React from "react";
import { getInfoRenderItem } from "./config";
import { transTimeFormat } from "lib/utils";
import Url from "lib/url-parser";
import { connect } from "react-redux";
import { Dispatch } from "redux";
import * as actions from "actions";
import "./index.less";
import { PageHeader, Button, Descriptions, Steps, Divider, Tooltip, Spin, message } from "antd";
import { IOrderInfo, ITypeEnums } from "typesPath/cluster/order-types";
import { IUser } from "typesPath/user-types";
import { cancelOrder, getOrderDetail, getTypeEnums } from "api/order-api";
import { IStringMap } from "interface/common";
import { MyApprovalPermissions, MyApplyPermissions } from "constants/permission";
import { hasOpPermission } from "lib/permission";
import { getRegionNodeSpec } from "api/op-cluster-region-api";
const { Step } = Steps;

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});
const connects: any = connect;
@connects(null, mapDispatchToProps)
export class OrderDetail extends React.Component<{
  setModalId: Function;
  setDrawerId: Function;
}> {
  public result: boolean;
  public orderId: number;

  public state = {
    showUnfold: false,
    loading: false,
    typeEnums: {} as IStringMap,
    orderInfo: {} as IOrderInfo,
  };

  constructor(props) {
    super(props);
    const url = Url();
    this.orderId = Number(url.search.orderId);
  }

  public dealInfo(info: IOrderInfo) {
    info = JSON.parse(JSON.stringify(info));
    const { detail = "", approverList = [], status } = info;

    info.detailInfo = detail ? JSON.parse(detail) : {};
    info.approvers = approverList.filter((item) => item.userName).map((item) => item.userName);
    info.applicant = info.applicant || ({} as IUser);
    info.currentStep = status === 0 ? 1 : status === 1 || status === 2 ? 2 : 0; // 0待审批 1已通过 2已驳回 3已撤回
    return info;
  }

  public renderApplicant() {
    const { applicant, createTime, applicantAppName } = this.state.orderInfo;
    const infoList = [
      {
        label: "申请人",
        value: applicant?.userName,
      },
      {
        label: "申请时间",
        value: this.formatTime(createTime),
      },
      {
        label: "应用名称",
        value: applicantAppName,
      },
    ];

    return (
      <>
        <Divider />
        <Descriptions title="申请人信息" column={3}>
          {infoList.map((item, key) => (
            <Descriptions.Item key={key} label={item.label}>
              <Tooltip placement="bottomLeft" title={item.value}>
                <span>{item.value || "-"}</span>
              </Tooltip>
            </Descriptions.Item>
          ))}
        </Descriptions>
      </>
    );
  }

  public renderDetail() {
    const { type } = this.state.orderInfo;
    const infoList = getInfoRenderItem(this.state.orderInfo);

    return (
      <>
        <Divider />
        <Descriptions title={`申请内容-${this.state.typeEnums[type] || ""}`} column={3}>
          {infoList.map((item, key) => (
            <Descriptions.Item key={key} label={item.label}>
              <Tooltip placement="bottomLeft" title={item.value}>
                <span>{item.value || "-"}</span>
              </Tooltip>
            </Descriptions.Item>
          ))}
        </Descriptions>
      </>
    );
  }

  public formatTime(time) {
    return <>{transTimeFormat(time)}</>;
  }

  public componentDidMount() {
    this.getOrderDetail();
    this.getTypeEnumsFn();
  }

  public getOrderDetail = () => {
    this.setState({ loading: true });
    getOrderDetail(this.orderId).then(async (data = {}) => {
      const { type, detail } = data;
      if (type && type === "logicClusterIndecrease" && detail) {
        const details = JSON.parse(detail);
        await getRegionNodeSpec(details.logicClusterId).then((res) => {
          details.dataNodeSpec = res;
          data.detail = JSON.stringify(details);
        });
      }
      this.setState({
        orderInfo: this.dealInfo(data),
        loading: false,
      });
    });
  };

  public getTypeEnumsFn = () => {
    getTypeEnums().then((res) => {
      const obj = {} as IStringMap;
      res.forEach((item: ITypeEnums) => {
        obj[item.type] = item.message;
      });
      this.setState({
        typeEnums: obj,
      });
    });
  };

  public cancelOrder(orderId: number) {
    cancelOrder(orderId).then(() => {
      message.success("撤回成功");
      this.getOrderDetail();
    });
  }

  public handerUnfold = () => {
    this.setState({
      showUnfold: !this.state.showUnfold,
    });
  };

  public formatApprovers = (approvers: string[], approverArr = []) => {
    if (approvers.length > 3) {
      approverArr.push(approvers.splice(0, 3));
      if (approvers.length) this.formatApprovers(approvers, approverArr);
    } else {
      approverArr.push(approvers);
    }
    return approverArr;
  };

  public renderApprovers = (approvers: string[]) => {
    const arrApprovers = this.formatApprovers([...approvers]);
    let unfoldList = [];
    if (arrApprovers.length > 2) unfoldList = arrApprovers.splice(2);

    return (
      <div>
        {arrApprovers.map((item, index) => (
          <div key={index}>{item?.join(", ")}</div>
        ))}
        {this.state.showUnfold ? unfoldList.map((item, index) => <div key={index}>{item?.join(", ")}</div>) : null}
        {unfoldList.length ? <a onClick={this.handerUnfold}>{this.state.showUnfold ? "收起" : "展开"}</a> : ""}
      </div>
    );
  };

  public render() {
    const isOrder = window.location.href.includes("my-application");
    const info = this.state.orderInfo;
    this.result = info.approverList?.some((item) => item.userName === info.applicant?.userName);

    return (
      <div className="order-detail">
        <Spin spinning={this.state.loading}>
          <div className="detail-top">
            <PageHeader
              className="btn-groups"
              title={`${info.title}${info.id ? `（${info.id}）` : ""}`}
              extra={
                info.currentStep === 1 || this.result ? (
                  <>
                    {isOrder && info.status === 0 ? (
                      <span key="4">
                        {hasOpPermission(MyApplyPermissions.PAGE, MyApplyPermissions.CALLBACK) && (
                          <Button key="5" type="primary" onClick={() => this.cancelOrder(info.id)}>
                            撤回
                          </Button>
                        )}
                      </span>
                    ) : info.status === 1 || info.status === 2 || info.status === 3 ? null : (
                      <span key="3">
                        {hasOpPermission(MyApprovalPermissions.PAGE, MyApprovalPermissions.DNOE) && (
                          <Button
                            key="1"
                            type="primary"
                            className="detail-btn"
                            onClick={() => {
                              if (info.type === "logicClusterIndecrease") {
                                this.props.setDrawerId("showApprovalDrawer", { ...info, outcome: "agree" }, this.getOrderDetail);
                              } else {
                                this.props.setModalId("showApprovalModal", { ...info, outcome: "agree" }, this.getOrderDetail);
                              }
                            }}
                          >
                            通过
                          </Button>
                        )}
                        {hasOpPermission(MyApprovalPermissions.PAGE, MyApprovalPermissions.CALLBACK) && (
                          <Button
                            key="2"
                            onClick={() =>
                              this.props.setModalId("showApprovalModal", { ...info, outcome: "disagree" }, this.getOrderDetail)
                            }
                          >
                            驳回
                          </Button>
                        )}
                      </span>
                    )}
                  </>
                ) : null
              }
            />
          </div>

          <div className="work-detail-box">
            <Steps
              className="step"
              current={info.currentStep}
              status={info.currentStep === 2 && info.status === 2 ? "error" : "process"}
              progressDot={true}
            >
              <Step
                title={`${this.state.typeEnums[info.type] || ""}${info.status === 3 ? "（已撤回）" : ""}`}
                subTitle={info.applicant?.userName}
                description={this.formatTime(info.createTime)}
              />
              <Step title="待审批" description={info.status === 0 ? this.renderApprovers(info.approvers) : null} />
              <Step
                title={info.status === 1 ? "已通过" : info.status === 2 ? "已拒绝" : "完成"}
                // subTitle={info.currentStep === 2 ? this.renderApprovers(info.approvers) : null}
                description={info.currentStep === 2 ? this.formatTime(info.finishTime) : null}
              />
            </Steps>

            {this.renderApplicant()}

            {info.detail ? this.renderDetail() : null}

            {info.status === 1 || info.status === 2 ? (
              <>
                <Divider />
                <Descriptions title="审批信息" column={1}>
                  <Descriptions.Item label="审批意见">{info.opinion}</Descriptions.Item>
                </Descriptions>
              </>
            ) : null}
          </div>
        </Spin>
      </div>
    );
  }
}
