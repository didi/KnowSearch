import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { regClusterName } from "constants/reg";
import { StaffSelect } from "container/staff-select";
import { AddRole, Masternode, RenderText } from "container/custom-form";
import {
  ICreatePhyCluster,
  INode,
  IRoleCluster,
  IRoleClusterHots,
} from "@types/cluster/cluster-types";
import { PHY_CLUSTER_TYPE, PHY_NODE_TYPE } from "constants/status-map";
import { IWorkOrder } from "@types/params-types";
import { AppState, UserState } from "store/type";
import { submitWorkOrder } from "api/common-api";
import { staffRuleProps } from "constants/table";
import Item from "antd/lib/list/Item";

export interface INodeListObjet {
  masternode: INode[];
  clientnode: INode[];
  datanode: INode[];
  datanodeceph: INode[];
}

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
  app: state.app,
  user: state.user,
});

const ApplyPhyClusterModal = (props: {
  dispatch: any;
  cb: Function;
  app: AppState;
  user: UserState;
  params: any;
}) => {
  const xFormModalConfig = {
    formMap: [
      {
        key: "project",
        label: "所属项目",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.app.appInfo()?.name} />,
      },
      {
        key: "type",
        label: "集群类型",
        type: FormItemType.select,
        defaultValue: 4,
        options: PHY_CLUSTER_TYPE,
        attrs: {
          style: { width: "60%" },
          disabled: true,
        },
        rules: [
          {
            required: true,
            message: "请选择",
            validator: async (rule: any, value: string) => {
              // updateFormModal(value);
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "cluster",
        label: "集群名称",
        attrs: {
          placeholder: "请填写集群名称",
          style: { width: "60%" },
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (!value || !new RegExp(regClusterName).test(value) || value?.trim().length > 128) {
                return Promise.reject("请填写正确集群名称，支持大、小写字母、数字、-、_1-128位字符");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      // {
      //   key: "creator",
      //   label: "责任人",
      //   colSpan: 10,
      //   rules: [
      //     {
      //       required: true,
      //       ...staffRuleProps,
      //     },
      //   ],
      //   type: FormItemType.custom,
      //   isCustomStyle: true,
      //   customFormItem: <StaffSelect style={{ width: "60%" }} />,
      // },
      {
        key: "esVersion",
        label: "ES版本",
        type: FormItemType.select,
        options: props.params?.packageDockerList || [],
        rules: [
          {
            required: true,
            message: "请选择",
          },
        ],
        attrs: {
          placeholder: "请选择版本",
          style: { width: "60%" },
        },
      },
      {
        key: "nsTree",
        label: "机器节点",
        rules: [
          {
            required: true,
            message: "请输入机器节点",
          },
        ],
        attrs: {
          placeholder: "请输入例如：arius-es-test.data-online.fd.es.com",
        },
      },
      {
        key: "masternode",
        label: "Masternode",
        type: FormItemType.custom,
        customFormItem: <Masternode nodeList={props.params?.nodeList || []} />,
        rules: [
          {
            required: true,
            whitespace: true,
            validator: async (rule: any, value) => {
              if (value) {
                return Promise.resolve();
              }
              return Promise.reject();
            },
          },
        ],
      },
      {
        key: "desc",
        type: FormItemType.textArea,
        label: "集群描述",
        rules: [
          {
            required: false,
            message: "请输入0-100个字描述信息",
            validator: async (rule: any, value: string) => {
              if (!value || value?.trim().length <= 100) {
                return Promise.resolve();
              } else {
                return Promise.reject();
              }
            },
          },
        ],
        attrs: {
          placeholder: '请输入集群描述'
        }
      },
      {
        key: "description",
        type: FormItemType.textArea,
        label: "申请原因",
        rules: [
          {
            required: true,
            message: "请输入1-100个字申请原因",
            validator: async (rule: any, value: string) => {
              if (value?.trim().length > 0 && value?.trim().length <= 100) {
                return Promise.resolve();
              } else {
                return Promise.reject();
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入申请原因",
        },
      },
    ] as IFormItem[],
    visible: true,
    title: "新建集群",
    formData: {},
    width: 660,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      // result.creator = Array.isArray(result.creator)
      //   ? result.creator.join(",")
      //   : result.creator;
      let contentObj = {} as ICreatePhyCluster;
      if (result.type === 3) {
        let roleClusters = [
          {
            role: "masternode",
            podNumber: result.masternode?.masterNodeNu,
            machineSpec: result.masternode?.masterSpec,
            pidCount: null,
          },
        ] as IRoleCluster[];
        let arr = [];
        if (result.masternode.nodeList && result.masternode.nodeList.length) {
          for (let index = 0; index < 2; index++) {
            let obj = {};
            if (result.masternode.nodeList[index]) {
              obj = {
                role: result.masternode.nodeList[index].node,
                podNumber: result.masternode.nodeList[index].nodeNu,
                machineSpec: result.masternode.nodeList[index].spec,
                pidCount: null,
              };
            } else {
              obj = {
                role: arr[0].role === "clientnode" ? "datanode" : "clientnode",
                podNumber: "",
                machineSpec: "",
                pidCount: null,
              };
            }
            arr.push(obj);
          }
          roleClusters = [...roleClusters, ...arr];
        } else {
          let arr = [
            {
              role: "datanode",
              podNumber: null,
              machineSpec: null,
              pidCount: null,
            },
            {
              role: "clientnode",
              podNumber: null,
              machineSpec: null,
              pidCount: null,
            },
          ] as IRoleCluster[];
          roleClusters = [...roleClusters, ...arr];
        }
        contentObj = {
          type: 3,
          phyClusterName: result.cluster,
          dataCenter: result.dataCenter,
          nsTree: result.nsTree,
          esVersion: result.esVersion,
          // creator: result.creator,
          desc: result.desc,
          roleClusters,
        };
      } else {
        const roleClusterHosts = [];
        // if (result.masternode.length === 1) {
        //   result.masternode?.forEach((element) => {
        //     PHY_NODE_TYPE.forEach((item) => {
        //       roleClusterHosts.push(...getPhyNodes(item, element.value));
        //     });
        //   });
        // } else if (result.masternode.length > 1) {
        //   result.masternode?.forEach((element) => {
        //     roleClusterHosts.push(...getPhyNodes(element.type, element.value));
        //   });
        // }
        if(result.masternode?.length) {
          result.masternode?.forEach((element) => {
            roleClusterHosts.push(...getPhyNodes(element.type, element.value));
          });
        }
        // const roleClusterHosts = master?.concat(client).concat(data) as IRoleClusterHots[];
        const spec = result.machineSpec;
        contentObj = {
          type: 4,
          phyClusterName: result.cluster,
          dataCenter: result.dataCenter,
          nsTree: result.nsTree,
          esVersion: result.esVersion,
          // creator: result.creator,
          desc: result.desc,
          pidCount: result.pidCount,
          roleClusterHosts: roleClusterHosts
        };
      }

      const params: IWorkOrder = {
        contentObj,
        submitorAppid: props.app.appInfo()?.id,
        submitor: (props.user as any)?.name,
        description: result.description || "",
        type: "clusterOpNew",
      };
      return submitWorkOrder(params, () => {
        props.dispatch(actions.setModalId(""));
      });
    },
  };

  const getPhyNodes = (node: string, value: string) => {
    const values = value?.split("\n").filter((ele: string) => ele !== "");
    const data = values?.map((ele: string) => ({
      role: node,
      address: ele.trim(),
    }));
    return data?.length ? data : [{ role: node, hostname: "" }];
  };

  const hostItem = [
    // {
    //   key: "pidCount",
    //   label: "单节点实例数",
    //   defaultValue: "1",
    //   rules: [
    //     {
    //       message: "请输入",
    //     },
    //   ],
    //   type: FormItemType.inputNumber,
    //   attrs: {
    //     placeholder: "请输入",
    //     disabled: true,
    //     style: { width: "60%" },
    //   },
    // },
    {
      key: "masternode",
      label: "Masternode",
      type: FormItemType.custom,
      customFormItem: <AddRole isHost={false}/>,
      rules: [
        {
          required: true,
          message: "",
          validator: (rule: any, value: any) => {
            if(!value?.length) {
              (window as any).masternodeErr = true;
              return Promise.reject("");;
            }
            (window as any).masternodeErr = false;
            const check = value.filter((item) => item.check);
            
            if (check.length) {
              return Promise.reject("");
            }
            
            const flag = value.some(item => !item.value);

            if(flag) {
              return Promise.reject("");
            }
            
            return Promise.resolve();
          },
        },
      ],
    },
  ] as IFormItem[];

  const dockerItem = [
    {
      key: "nsTree",
      label: "机器节点",
      rules: [
        {
          required: true,
          message: "请输入机器节点",
        },
      ],
      attrs: {
        placeholder: "请输入例如：arius-es-test.data-online.fd.es.com",
      },
    },
    {
      key: "masternode",
      label: "Masternode",
      type: FormItemType.custom,
      customFormItem: <Masternode nodeList={props.params?.nodeList || []} />,
      rules: [
        {
          required: true,
          whitespace: true,
          validator: async (rule: any, value: any) => {
            if (value) {
              return Promise.resolve();
            }
            return Promise.reject();
          },
        },
      ],
    },
  ] as IFormItem[];

  const esVersionHost = {
    key: "esVersion",
    label: "ES版本",
    type: FormItemType.select,
    options: props.params?.packageHostList || [],
    rules: [
      {
        required: true,
        message: "请选择",
      },
    ],
    attrs: {
      placeholder: "请选择版本",
      style: { width: "60%" },
    },
  } as IFormItem;

  const esVersionDocker = {
    key: "esVersion",
    label: "ES版本",
    type: FormItemType.select,
    options: props.params?.packageDockerList || [],
    rules: [
      {
        required: true,
        message: "请选择",
      },
    ],
    attrs: {
      placeholder: "请选择版本",
      style: { width: "60%" },
    },
  } as IFormItem;

  // 默认选中 host，更新选中 host 后的配置项
  // xFormModalConfig.formMap.splice(5, 2, ...hostItem);
  // xFormModalConfig.formMap.splice(4, 1, esVersionHost);
  // 物理集群-新建集群，责任人暂时注释，剪切下标需要减一
  xFormModalConfig.formMap.splice(4, 2, ...hostItem);
  xFormModalConfig.formMap.splice(3, 1, esVersionHost);

  // 这是一段比较恶心的代码 xform的值无法传下去 但是内部需要接收到
  React.useEffect(() => {
    return () => {
      (window as any).masternodeErr = false;
      delete (window as any).masternodeErr;
    }
  },[])

  const updateFormModal = (type) => {
    if (type === 4) {
      xFormModalConfig.formMap.splice(5, 2, ...hostItem);
      xFormModalConfig.formMap.splice(4, 1, esVersionHost);
    } else {
      xFormModalConfig.formMap.splice(5, 2, ...dockerItem);
      xFormModalConfig.formMap.splice(4, 1, esVersionDocker);
    }
    $ref.current?.updateFormMap$(xFormModalConfig.formMap, {});
  };

  const $ref: any = React.createRef();

  const { loading } = props.params;

  return (
    <>
      {!loading ? (
        <XFormWrapper ref={$ref} visible={true} {...xFormModalConfig} />
      ) : (
        <span>loading...</span>
      )}
    </>
  );
};

export default connect(mapStateToProps)(ApplyPhyClusterModal);
