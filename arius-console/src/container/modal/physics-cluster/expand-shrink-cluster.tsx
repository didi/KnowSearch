import { message, Modal } from 'antd';
import React from "react";
import { connect } from "react-redux";
import {
  FormItemType,
  IFormItem,
  XForm as XFormComponent,
} from "component/x-form";
import { ExpandShrinkNodeList } from "container/custom-form";
import "./index.less";
import { IOpExpandValues, IRoleIpList } from "typesPath/cluster/cluster-types";
import * as actions from "actions";
import {
  CLUSTER_INDECREASE_TYPE,
  VERSION_MAINFEST_TYPE,
} from "constants/status-map";
import { IWorkOrder } from "typesPath/params-types";
import { submitWorkOrder } from "api/common-api";

const labelList = [
  {
    label: "集群名称：",
    key: "cluster",
  },
  {
    label: "集群类型：",
    key: "type",
    render: (type: number) => <>{VERSION_MAINFEST_TYPE[type]}</>,
  },
  {
    label: "现有ES版本：",
    key: "esVersion",
  },
];

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});
const connects: Function = connect
@connects(mapStateToProps, null)
export class ExpandShrinkCluster extends React.Component<any> {
  public state = {
    opValues: {} as any,
    masterNodes: "",
    clientNodes: "",
    dataNodes: "",
    coldNodes: "",
    formMap: [
      {
        key: "type",
        label: "扩缩容",
        type: FormItemType.select,
        defaultValue: 3,
        rules: [
          {
            required: true,
            validator: (rule: any, value: any) => {
              if (value === 2) {
                this.getFormMap(true);
              } else {
                this.getFormMap(false);
              }
              return Promise.resolve();
            },
          },
        ],
        options: CLUSTER_INDECREASE_TYPE,
        attrs: {
          style: { width: "50%" },
        },
      },
    ] as IFormItem[],
  };

  public componentDidMount() {
    const { esRoleClusterVOS = [] } = this.props.params;
    // TODO: 如果 esRoleClusterVOS 不存在，会影响页面功能
    esRoleClusterVOS?.forEach((item) => {
      switch (item.role) {
        case "masternode":
          this.setState(
            {
              masterNodes: item.esRoleClusterHostVO.map((i) => i.ip).join("\n"),
            },
            () => {
              this.getFormMap(false);
            }
          );
          break;
        case "clientnode":
          this.setState(
            {
              clientNodes: item.esRoleClusterHostVO.map((i) => i.ip).join("\n"),
            },
            () => {
              this.getFormMap(false);
            }
          );
          break;
        case "datanode":
          this.setState(
            {
              dataNodes: item.esRoleClusterHostVO.filter((i) => i.rack !== 'cold').map((i) => i.ip).join("\n"),
              coldNodes: item.esRoleClusterHostVO.filter((i) => i.rack === 'cold').map((i) => i.ip).join("\n"),
            },
            () => {
              this.getFormMap(false);
            }
          );
          break;
        default:
          break;
      }
    });
  }

  public getPhyNodeRules = (node: string, value: string, opType?: number) => {
    const { opValues } = this.state;
    let originArr = [];
    const valueArr = value
      ? value
        ?.split("\n")
        .filter((ele: string) => ele !== "")
        .map((ele: string) => ele.trim())
      : [];
    let master = opValues?.masterNode ? opValues.masterNode?.split("\n") : [];
    let client = opValues?.clientNode ? opValues.clientNode?.split("\n") : [];
    let data = opValues?.dataNode ? opValues.dataNode?.split("\n") : [];
    if (opType && opType === 3) {
      master = opValues?.masterNode ? opValues.masterNode?.split("\n") : [];
      client = opValues?.clientNode ? opValues.clientNode?.split("\n") : [];
      data = opValues?.dataNode ? opValues.dataNode?.split("\n") : [];
    } else {
      master = opValues?.masterNodeExpand
        ? opValues.masterNodeExpand?.split("\n")
        : [];
      client = opValues?.clientNodeExpand
        ? opValues.clientNodeExpand?.split("\n")
        : [];
      data = opValues?.dataNodeExpand
        ? opValues.dataNodeExpand?.split("\n")
        : [];
    }

    let otherValue = [] as string[];
    switch (node) {
      case "masterNode":
        otherValue = client.concat(data);
        originArr = this.state.masterNodes.split("\n") || [];
        break;
      case "clientNode":
        otherValue = master.concat(data);
        originArr = this.state.clientNodes.split("\n") || [];
        break;
      case "dataNode":
        otherValue = master.concat(client);
        originArr = this.state.dataNodes.split("\n") || [];
        break;
      case "coldNode":
        otherValue = master.concat(client);
        originArr = this.state.coldNodes.split("\n") || [];
        break;
    }

    if (!valueArr.length) {
      if (node !== "client") {
        return `请输入${node}列表`;
      }
      return true;
    } else {
      let repeat = null as boolean;
      otherValue.forEach((ele: string) => {
        if (valueArr.includes(ele)) {
          repeat = true;
        }
      });
      if (!opType) {
        originArr.forEach((ele: string) => {
          if (valueArr.includes(ele)) {
            repeat = true;
          }
        });
      }
      const judge = new Set(valueArr).size !== valueArr.length;
      if (repeat || judge) {
        return "存在重复主机，请调整主机角色或修改IP";
      } else if (opType === 3) {
        if (originArr?.length < valueArr?.length) {
          return `缩容为减少主机数，请输入小于原有${node}列表的主机数`;
        } else {
          let judge = null as boolean;
          valueArr.forEach((ele: string) => {
            if (!originArr.includes(ele)) {
              judge = true;
            }
          });
          if (judge) {
            return `请在当前${node}列表主机进行缩容， 不可减少主机后增加其它主机`;
          }
          return true;
        }
      } else {
        return true;
      }
    }
  };

  formRef: any = React.createRef();

  strSplitSortJoin = (str: string) => {
    return str ? str.split('\n').sort().join('') : '';
  }

  public handleOk = () => {
    this.formRef.current!.validateFields().then((result) => {
      let flag = false;
      // 扩容
      if (result.type === 2) {
        // 如果数据都为空判断没有进行扩容操作数据没有更改
        if(!result.clientNodeExpand && !result.dataNodeExpand && !result.coldNodeExpand) {
          flag = true;
        }
      } else {
        // 如果 clientNodes 和 dataNodes 为空，则无法进行所容，不能点击确定按钮
        if (!this.state.clientNodes && !this.state.dataNodes && !this.state.coldNodes) {
          flag = true;
        } else {
          // 将字符串 转成数组 再排序 最后转换成字符串 判断是否相同
          const oldClientNode = this.strSplitSortJoin(this.state.clientNodes)
          const oldDataNode = this.strSplitSortJoin(this.state.dataNodes + `\n${this.state.coldNodes}`);
          const clientNode = this.strSplitSortJoin(result.clientNode);
          const dataNode = this.strSplitSortJoin(result.dataNode + `\n${result.coldNode}`);
          // 判断前后数据是否相同
          if(oldClientNode === clientNode && oldDataNode === dataNode) {
            flag = true;
          }
        }
      }

      if(flag) {
        message.error('请对数据进行更改后再提交');
        return;
      }

      let roleClusterHosts = [] as IRoleIpList[];
      const masterArr = [] as IRoleIpList[];
      const clientArr = [] as IRoleIpList[];
      const dataArr = [] as IRoleIpList[];
      const coldArr = [] as IRoleIpList[];
      const masterBaseArr = this.state.masterNodes.split("\n");
      const clientBaseArr = this.state.clientNodes.split("\n");
      const dataBaseArr = this.state.dataNodes.split("\n");
      const coldBaseArr = this.state.coldNodes.split("\n");
      let masterExpandArr = result?.masterNode
        ?.split("\n")
        .filter((ele: string) => ele !== "")
        .map((ele: string) => ele.trim());
      let clientExpandArr = result?.clientNode
        ?.split("\n")
        .filter((ele: string) => ele !== "")
        .map((ele: string) => ele.trim());
      let dataExpandArr = result?.dataNode
        ?.split("\n")
        .filter((ele: string) => ele !== "")
        .map((ele: string) => ele.trim());
      let coldExpandArr = result?.coldNode
        ?.split("\n")
        .filter((ele: string) => ele !== "")
        .map((ele: string) => ele.trim());
      if (result?.type === 2) {
        masterExpandArr = result?.masterNodeExpand
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
        clientExpandArr = result?.clientNodeExpand
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
        dataExpandArr = result?.dataNodeExpand
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
        coldExpandArr = result?.coldNodeExpand
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
      } else {
        masterExpandArr = result?.masterNode
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
        clientExpandArr = result?.clientNode
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
        dataExpandArr = result?.dataNode
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
          coldExpandArr = result?.coldNode
          ?.split("\n")
          .filter((ele: string) => ele !== "")
          .map((ele: string) => ele.trim());
      }
      if (result?.type === 2) {
        masterExpandArr && masterExpandArr.forEach((ele: string) => {
          if (!masterBaseArr.includes(ele)) {
            masterArr.push({ role: "masternode", hostname: ele, beCold: false });
          }
        });
        clientExpandArr && clientExpandArr.forEach((ele: string) => {
          if (!clientBaseArr.includes(ele)) {
            clientArr.push({ role: "clientnode", hostname: ele, beCold: false });
          }
        });
        dataExpandArr && dataExpandArr.forEach((ele: string) => {
          if (!dataBaseArr.includes(ele)) {
            dataArr.push({ role: "datanode", hostname: ele, beCold: false });
          }
        });
        coldExpandArr && coldExpandArr.forEach((ele: string) => {
          if (!coldBaseArr.includes(ele)) {
            coldArr.push({ role: "datanode", hostname: ele, beCold: true });
          }
        });
      } else {
        masterExpandArr && masterBaseArr.forEach((ele: string) => {
          if (ele && !masterExpandArr.includes(ele)) {
            masterArr.push({ role: "masternode", hostname: ele, beCold: false });
          }
        });
        clientBaseArr && clientBaseArr.forEach((ele: string) => {
          if (ele && !clientExpandArr.includes(ele)) {
            clientArr.push({ role: "clientnode", hostname: ele, beCold: false });
          }
        });
        dataBaseArr && dataBaseArr.forEach((ele: string) => {
          if (ele && !dataExpandArr.includes(ele)) {
            dataArr.push({ role: "datanode", hostname: ele, beCold: false });
          }
        });
        coldBaseArr && coldBaseArr.forEach((ele: string) => {
          if (ele && !coldExpandArr.includes(ele)) {
            coldArr.push({ role: "datanode", hostname: ele, beCold: true });
          }
        });
      }
      const master = masterArr?.length
        ? masterArr
        : [{ role: "masternode", hostname: "", beCold: false }];
      const client = clientArr?.length
        ? clientArr
        : [{ role: "clientnode", hostname: "", beCold: false }];
      const data = dataArr?.length
        ? dataArr
        : [{ role: "datanode", hostname: "", beCold: false}];
      const cold = coldArr?.length
        ? coldArr
        : [];

      // TODO: masterNode 暂不支持扩所容
      // roleClusterHosts = master.concat(client).concat(data);
      roleClusterHosts = client.concat(data);
      roleClusterHosts = roleClusterHosts.concat(cold);

      let originRoleClusterHosts = [] as any[];
      const originMasterArr = masterBaseArr.map((ele) => ({
        role: "masternode",
        hostname: ele,
      }));
      const originClientArr = clientBaseArr.map((ele) => ({
        role: "clientnode",
        hostname: ele,
      }));
      const originDataArr = dataBaseArr.map((ele) => ({
        role: "datanode",
        hostname: ele,
      }));

      // originRoleClusterHosts = originMasterArr
      //   .concat(originClientArr)
      //   .concat(originDataArr);
      // TODO: masterNode 暂不支持扩所容
      originRoleClusterHosts = originClientArr
        .concat(originDataArr);

      const contentObj = {
        type: 4,
        operationType: result?.type,
        phyClusterId: this.props.params.id,
        phyClusterName: this.props.params.cluster,
        roleClusterHosts,
        originRoleClusterHosts,
      };
      const params: IWorkOrder = {
        contentObj,
        submitorAppid: this.props.app.appInfo()?.id,
        submitor: this.props.user.getName('domainAccount'),
        description: result.description || "",
        type: "clusterOpIndecrease",
      };
      submitWorkOrder(params, () => {
        this.props.dispatch(actions.setModalId(""));
      });
    });
  };

  public handleCancel = () => {
    this.props.dispatch(actions.setModalId(""));
  };

  isNode = () => {
    const { clientNode, dataNode } = this.formRef.current!.getFieldsValue(true);
    // 判断 master 列表、client 列表、data 列表是否都为空
    return  !this.state.masterNodes && !clientNode && !dataNode;
  }  

  public getFormMap = (b: boolean) => {
    const shrinkArr = [
      {
        key: "masterNode",
        label: "master列表",
        // type: FormItemType.textArea,
        // defaultValue: this.state.masterNodes,
        type: FormItemType.custom,
        customFormItem: <ExpandShrinkNodeList host={this.state.masterNodes} isHidden={true} />,
        rules: [
          {
            validator: (rule: any, value: any) => {
              // if(this.isNode()) {
              //   return Promise.reject('至少保留一个节点');
              // }
              if(!value) {
                return Promise.resolve();
              }
              if (this.getPhyNodeRules("masterNode", value)) {
                if (
                  typeof this.getPhyNodeRules("masterNode", value, 3) ===
                  "string"
                )
                  return Promise.reject(
                    this.getPhyNodeRules("masterNode", value, 3)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        attrs: {
          placeholder: `请输入主机列表，多个主机换行`,
          rows: 4,
        },
      },
      {
        key: "clientNode",
        label: "client列表",
        type: FormItemType.textArea,
        defaultValue: this.state.clientNodes,
        rules: [
          {
            validator: (rule: any, value: any) => {
              if(this.isNode()) {
                return Promise.reject('至少保留一个节点');
              }
              if(!value) {
                return Promise.resolve();
              }
              if(!this.state.clientNodes) {
                return Promise.reject("client列表主机不存在，无法进行缩容操作");
              }
              if (this.getPhyNodeRules("clientNode", value)) {
                if (
                  typeof this.getPhyNodeRules("clientNode", value, 3) ===
                  "string"
                )
                  return Promise.reject(
                    this.getPhyNodeRules("clientNode", value, 3)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        attrs: {
          placeholder: `请输入主机列表，多个主机换行`,
          rows: 4,
        },
      },
      {
        key: "dataNode",
        label: "data列表",
        type: FormItemType.textArea,
        defaultValue: this.state.dataNodes,
        rules: [
          {
            validator: (rule: any, value: any) => {
              if(this.isNode()) {
                return Promise.reject('至少保留一个节点');
              }
              if(!value) {
                return Promise.resolve();
              }
              if(!this.state.dataNodes) {
                return Promise.reject("data列表主机不存在，无法进行缩容操作");
              }
              if (this.getPhyNodeRules("dataNode", value)) {
                if (
                  typeof this.getPhyNodeRules("dataNode", value, 3) === "string"
                )
                  return Promise.reject(
                    this.getPhyNodeRules("dataNode", value, 3)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        attrs: {
          placeholder: `请输入主机列表，多个主机换行`,
          rows: 4,
        },
      },
      {
        key: "coldNode",
        label: "cold列表",
        type: FormItemType.textArea,
        defaultValue: this.state.coldNodes,
        rules: [
          {
            validator: (rule: any, value: any) => {
              if(this.isNode()) {
                return Promise.reject('至少保留一个节点');
              }
              if(!value) {
                return Promise.resolve();
              }
              if(!this.state.coldNodes) {
                return Promise.reject("cold列表主机不存在，无法进行缩容操作");
              }
              if (this.getPhyNodeRules("coldNode", value)) {
                if (
                  typeof this.getPhyNodeRules("coldNode", value, 3) === "string"
                )
                  return Promise.reject(
                    this.getPhyNodeRules("coldNode", value, 3)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        attrs: {
          placeholder: `请输入主机列表，多个主机换行`,
          rows: 4,
        },
      },
      {
        key: "description",
        type: FormItemType.textArea,
        label: "申请原因",
        rules: [
          {
            required: true,
            whitespace: true,
            validator: (rule: any, value: string) => {
              if (value?.trim().length > 0 && value?.trim().length <= 100) {
                return Promise.resolve();
              } else if (value?.trim().length > 50) {
                return Promise.reject('申请原因不能超过100字符');
              } else {
                return Promise.reject('申请原因不能为空');
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入1-100字申请原因",
        },
      },
    ] as IFormItem[];

    const expandArr = [
      {
        key: "masterNodeExpand",
        label: "master列表",
        type: FormItemType.custom,
        rules: [
          {
            validator: (rule: any, value: any) => {
              if(!value) {
                return Promise.resolve();
              }
              if (this.getPhyNodeRules("masterNode", value)) {
                if (
                  typeof this.getPhyNodeRules("masterNode", value) === "string"
                )
                  return Promise.reject(
                    this.getPhyNodeRules("masterNode", value)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        customFormItem: <ExpandShrinkNodeList host={this.state.masterNodes} isHidden={true} />,
      },
      {
        key: "clientNodeExpand",
        label: "client列表",
        type: FormItemType.custom,
        rules: [
          {
            validator: (rule: any, value: any) => {
              if(!value) {
                return Promise.resolve();
              }
              if (this.getPhyNodeRules("clientNode", value)) {
                if (
                  typeof this.getPhyNodeRules("clientNode", value) === "string"
                )
                  return Promise.reject(
                    this.getPhyNodeRules("clientNode", value)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        customFormItem: <ExpandShrinkNodeList host={this.state.clientNodes} />,
      },
      {
        key: "dataNodeExpand",
        label: "data列表",
        type: FormItemType.custom,
        rules: [
          {
            validator: (rule: any, value: any) => {
              if(!value) {
                return Promise.resolve();
              }
              if (this.getPhyNodeRules("dataNode", value)) {
                if (typeof this.getPhyNodeRules("dataNode", value) === "string")
                  return Promise.reject(
                    this.getPhyNodeRules("dataNode", value)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        customFormItem: <ExpandShrinkNodeList host={this.state.dataNodes} />,
      },
      {
        key: "coldNodeExpand",
        label: "cold属性节点列表",
        type: FormItemType.custom,
        rules: [
          {
            validator: (rule: any, value: any) => {
              if(!value) {
                return Promise.resolve();
              }
              if (this.getPhyNodeRules("coldNode", value)) {
                if (typeof this.getPhyNodeRules("coldNode", value) === "string")
                  return Promise.reject(
                    this.getPhyNodeRules("coldNode", value)
                  );
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
        customFormItem: <ExpandShrinkNodeList host={this.state.coldNodes} />,
      },
      {
        key: "description",
        type: FormItemType.textArea,
        label: "申请原因",
        rules: [
          {
            required: true,
            whitespace: true,
            validator: (rule: any, value: string) => {
              if (value?.trim().length > 0 && value?.trim().length < 100) {
                return Promise.resolve();
              } else {
                return Promise.reject('请输入1-100字申请原因');
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入1-100字申请原因",
        },
      },
    ] as IFormItem[];

    const arr = this.state.formMap;
    if (b) {
      arr.splice(1, 5, ...expandArr);
      this.setState({
        formMap: arr,
      });
    } else {
      arr.splice(1, 5, ...shrinkArr);
      this.setState({
        formMap: arr,
      });
    }
  };

  public onHandleValuesChange = (value: any, allValues: IOpExpandValues) => {
    Object.keys(value).forEach((key) => {
      switch (key) {
        case "operationType": //  2:扩容 3:缩容
          this.setState({
            opValues: allValues,
          });
          break;
        case "masterNode":
        case "clientNode":
        case "dataNode":
        case "masterNodeExpand":
        case "clientNodeExpand":
        case "dataNodeExpand":
          this.setState({
            opValues: allValues,
          });
          break;
      }
    });
  };

  render() {
    return (
      <>
        <Modal
          visible={true}
          title="集群扩缩容"
          width={660}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
          maskClosable={false}
          okText={'确定'}
          cancelText={'取消'}
        >
          <div className="upgrade-cluster-box">
            {labelList.map((item, index) => (
              <div
                key={item.label + index}
                className="upgrade-cluster-box-item"
              >
                <label htmlFor={item.label}>{item.label}</label>
                <span>
                  {item.render
                    ? item.render(this.props.params[item.key])
                    : this.props.params[item.key]}
                </span>
              </div>
            ))}
          </div>
          <div>
            <XFormComponent
              formData={{}}
              formMap={this.state.formMap}
              wrappedComponentRef={this.formRef}
              onHandleValuesChange={this.onHandleValuesChange}
              layout={"vertical"}
            />
          </div>
        </Modal>
      </>
    );
  }
}
