import { Modal } from 'antd';
import React from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import {
  FormItemType,
  IFormItem,
  XForm as XFormComponent,
} from "component/x-form";
import { OrderNode } from "container/custom-form";
import "./index.less";
import { VERSION_MAINFEST_TYPE } from "constants/status-map";
import { IVersions } from "@types/cluster/physics-type";
import { getPackageList } from "api/cluster-api";
import { IWorkOrder } from "@types/params-types";
import { submitWorkOrder } from "api/common-api";

const labelList = [
  {
    label: "集群名称：",
    key: "cluster",
    content: "",
  },
  {
    label: "申请人：",
    key: "user",
    content: "",
  },
  {
    label: "集群类型：",
    key: "type",
    content: "",
  },
  {
    label: "现有ES版本：",
    key: "esVersion",
    content: "",
  },
];

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) =>
    dispatch(actions.setModalId(modalId, params, cb)),
});

const mapStateToProps = state => ({
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
      {
        key: "description",
        type: FormItemType.textArea,
        label: "申请原因",
        rules: [
          {
            required: true,
            whitespace: true,
            validator: async (rule: any, value: string) => {
              if (value?.trim().length > 0 && value?.trim().length < 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入1-100字申请原因");
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入1-100字申请原因",
        },
      },
    ] as IFormItem[],
  };
  formRef: any = React.createRef();

  public componentDidMount() {
    const { params } = this.props;
    let packageDockerList = [] as IVersions[];
    let packageHostList = [] as IVersions[];
    getPackageList().then((data: IVersions[]) => {
      const list = data.filter((data, indx, self) => {
        return (
          self.findIndex((ele) => ele.esVersion === data.esVersion) === indx
        );
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
    this.formRef.current!.validateFields().then((result) => {
      result.roleOrder = result.roleOrder.map((item) => item.roleClusterName);
      const params: IWorkOrder = {
        contentObj: {
          phyClusterId: this.props.params.id,
          phyClusterName: this.props.params.cluster,
          esVersion: result.esVersion,
          roleOrder: result.roleOrder,
        },
        submitorAppid: this.props.app.appInfo()?.id,
        submitor: this.props.user.getName('domainAccount'),
        description: result.description || "",
        type: "clusterOpUpdate",
      };
      return submitWorkOrder(params);
    });
  };

  public handleCancel = () => {
    this.props.setModalId("");
  };

  public getLabelList = () => {
    const { params, user } = this.props;
    return labelList.map((item) => {
      item.content = params[item.key];
      if (item.key === "user") {
        item.content = user.getName('domainAccount');
      }
      if (item.key === "type") {
        item.content = VERSION_MAINFEST_TYPE[params[item.key]];
      }
      if (item.key === "user") {
        item.content = this.props.user.getName('domainAccount');
      }
      return item;
    });
  };

  render() {
    return (
      <div>
        <Modal
          visible={true}
          title="集群升级"
          width={660}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
          maskClosable={false}
          okText={'确定'}
          cancelText={'取消'}
        >
          <div className="upgrade-cluster-box">
            {this.getLabelList().map((item, index) => (
              <div
                key={item.label + index}
                className="upgrade-cluster-box-item"
              >
                <label htmlFor={item.label}>{item.label}</label>
                <span>{item.content}</span>
              </div>
            ))}
          </div>
          <div>
            <XFormComponent
              formData={{}}
              formMap={this.state.formMapItem}
              wrappedComponentRef={this.formRef}
              layout={"vertical"}
            />
          </div>
        </Modal>
      </div>
    );
  }
}
