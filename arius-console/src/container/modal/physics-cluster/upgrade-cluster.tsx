import { Drawer, Button } from "antd";
import React from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { OrderNode } from "container/custom-form";
import "./index.less";
import { VERSION_MAINFEST_TYPE } from "constants/status-map";
import { IVersions } from "typesPath/cluster/physics-type";
import { getPackageList } from "api/cluster-api";
import { clusterUpgrade } from "api/cluster-api";
import { showSubmitTaskSuccessModal } from "container/custom-component";

const labelList = [
  {
    label: "集群名称：",
    key: "cluster",
    content: "",
  },
  {
    label: "现有ES版本：",
    key: "esVersion",
    content: "",
  },
];

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setDrawerId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  params: state.modal.params,
  user: state.user,
  app: state.app,
});

const connects: Function = connect;

@connects(mapStateToProps, mapDispatchToProps)
export class UpgradeCluster extends React.Component<any> {
  state = {
    formMapItem: [
      {
        key: "esVersion",
        label: "升级至ES版本",
        type: FormItemType.select,
        options: [],
        rules: [
          {
            required: true,
          },
        ],
        attrs: {
          style: { width: "50%" },
        },
      },
      {
        key: "roleOrder",
        label: "重启节点顺序",
        type: FormItemType.custom,
        customFormItem: <OrderNode id={this.props.params?.id} />,
      },
    ] as IFormItem[],
    confirmLoading: false,
  };
  formRef: any = React.createRef();

  public componentDidMount() {
    const { params } = this.props;
    let packageDockerList = [] as IVersions[];
    let packageHostList = [] as IVersions[];
    getPackageList().then((data: IVersions[]) => {
      const list = data.filter((data, indx, self) => {
        return self.findIndex((ele) => ele.esVersion === data.esVersion) === indx;
      });
      const packageList = list.map((ele, index) => {
        return {
          ...ele,
          key: index,
          value: ele.esVersion,
        };
      });
      packageDockerList = packageList.filter((ele) => ele.manifest === 3);
      packageHostList = packageList.filter((ele) => ele.manifest === 4);
      const formMapItem = this.state.formMapItem;
      if (params.type === 3) {
        formMapItem[0].options = packageDockerList;
        this.setState({
          formMapItem,
        });
      } else {
        formMapItem[0].options = packageHostList;
        this.setState({
          formMapItem,
        });
      }
    });
  }

  public handleOk = () => {
    this.setState({ confirmLoading: true });
    this.formRef.current!.validateFields().then(async (result) => {
      result.roleOrder = result.roleOrder.map((item) => item.roleClusterName);
      let roleClusterHosts = [];
      if (this.props.params && this.props.params.esRoleClusterVOS) {
        this.props.params.esRoleClusterVOS?.forEach((item) => {
          item?.esRoleClusterHostVO?.forEach((obj) => {
            const param: any = { role: item.role, hostname: obj.hostname };
            if (obj.rack == "cold") {
              param.beCold = true;
            } else {
              param.beCold = false;
            }
            roleClusterHosts.push(param);
          });
        });
      }
      let params = {
        phyClusterId: this.props.params.id,
        phyClusterName: this.props.params.cluster,
        esVersion: result.esVersion,
        roleOrder: result.roleOrder,
        roleClusterHosts,
      };
      let expandData = JSON.stringify(params);
      try {
        let ret = await clusterUpgrade({ expandData });
        this.props.setDrawerId("");
        showSubmitTaskSuccessModal(ret, this.props.params?.history);
      } finally {
        this.setState({ confirmLoading: false });
      }
    });
  };

  public handleCancel = () => {
    this.props.setDrawerId("");
  };

  public getLabelList = () => {
    const { params, user } = this.props;
    return labelList.map((item) => {
      item.content = params[item.key];
      if (item.key === "user") {
        item.content = user.getName("userName");
      }
      if (item.key === "type") {
        item.content = VERSION_MAINFEST_TYPE[params[item.key]];
      }
      if (item.key === "user") {
        item.content = this.props.user.getName("userName");
      }
      return item;
    });
  };

  render() {
    return (
      <div>
        <Drawer
          destroyOnClose={true}
          onClose={this.handleCancel}
          maskClosable={false}
          closable={true}
          visible={true}
          title="集群升级"
          width={660}
          footer={
            <div className="footer-btn">
              <Button style={{ marginRight: 10 }} loading={this.state.confirmLoading} type="primary" onClick={this.handleOk}>
                确定
              </Button>
              <Button onClick={this.handleCancel}>取消</Button>
            </div>
          }
        >
          <div className="upgrade-cluster-box">
            {this.getLabelList().map((item, index) => (
              <div key={item.label + index} className="upgrade-cluster-box-item">
                <span className="label">{item.label}</span>
                <span>{item.content}</span>
              </div>
            ))}
          </div>
          <div className="upgrade-cluster-form">
            <XFormComponent formData={{}} formMap={this.state.formMapItem} wrappedComponentRef={this.formRef} layout={"vertical"} />
          </div>
        </Drawer>
      </div>
    );
  }
}
