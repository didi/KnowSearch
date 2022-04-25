import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { regClusterName } from "constants/reg";
import { StaffSelect } from "container/staff-select";
import { ICreatePhyCluster, INode, IRoleCluster, IRoleClusterHots } from "typesPath/cluster/cluster-types";
import { PHY_CLUSTER_TYPE, PHY_NODE_TYPE } from "constants/status-map";
import { IWorkOrder } from "typesPath/params-types";
import { AppState, UserState } from "store/type";
import { submitWorkOrder } from "api/common-api";
import { staffRuleProps } from "constants/table";
import Item from "antd/lib/list/Item";
import { AddRole, Masternode, RenderText } from "container/custom-form";
import { AddRoleTable, nodeTypeList } from "container/custom-form/add-role-table";
import { addRoleformMap } from "container/custom-form/tpl-table-add-row/editTable";

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

const ApplyPhyClusterModal = (props: { dispatch: any; cb: Function; app: AppState; user: UserState; params: any }) => {
  const getRoleClusterHosts = (result) => {
    console.log(result)
    // 聚合成接口请求的格式
    const masternodeRoleArr = [];
    const masternodeRoleArrKey = [];
    const clientnodeRoleArr = [];
    const clientnodeRoleArrKey = [];
    const datanodeRoleArr = [];
    const datanodeRoleArrKey = [];
    const coldRoleArr = [];
    const coldRoleArrKey = [];

    Object.keys(result).map((item) => {
      if (item.indexOf(nodeTypeList[0]) > -1) {
        const analysisKey = item.split("&");
        if (analysisKey.length !== 3) return;
        masternodeRoleArr.push({ role: analysisKey[0], [analysisKey[1]]: result[item], key: analysisKey[2], beCold: false });
        masternodeRoleArrKey.push(analysisKey[2]);
      }
      if (item.indexOf(nodeTypeList[1]) > -1) {
        const analysisKey = item.split("&");
        if (analysisKey.length !== 3) return;
        clientnodeRoleArr.push({ role: analysisKey[0], [analysisKey[1]]: result[item], key: analysisKey[2], beCold: false });
        clientnodeRoleArrKey.push(analysisKey[2]);
      }
      if (item.indexOf(nodeTypeList[2]) > -1) {
        const analysisKey = item.split("&");
        if (analysisKey.length !== 3) return;
        datanodeRoleArr.push({ role: analysisKey[0], [analysisKey[1]]: result[item], key: analysisKey[2], beCold: false });
        datanodeRoleArrKey.push(analysisKey[2]);
      }
      if (item.indexOf(nodeTypeList[3]) > -1) {
        const analysisKey = item.split("&");
        if (analysisKey.length !== 3) return;
        // 此处role使用datanode
        coldRoleArr.push({ role: nodeTypeList[2], [analysisKey[1]]: result[item], key: analysisKey[2], beCold: true });
        coldRoleArrKey.push(analysisKey[2]);
      }
    });
    const masternodeHosts = getRole(masternodeRoleArrKey, masternodeRoleArr);
    const clientnodeHosts = getRole(clientnodeRoleArrKey, clientnodeRoleArr);
    const datanodeHosts = getRole(datanodeRoleArrKey, datanodeRoleArr);
    const coldHosts = getRole(coldRoleArrKey, coldRoleArr);
    const roleClusterHosts = [...masternodeHosts, ...clientnodeHosts, ...datanodeHosts, ...coldHosts];
    return roleClusterHosts;
  };

  const getRole = (arrKey, RoleArr) => {
    const arr = [];
    new Set([...arrKey]).forEach((item) => {
      let obj: { [key: string]: any } = {};
      RoleArr.forEach((ele) => {
        if (item === ele.key) {
          obj = { ...obj, ...ele };
        }
      });
      const realDataStructure = {
        role: obj.role,
        address: obj[addRoleformMap[0].key],
        beCold: obj.beCold,
        machineSpec: `${obj[addRoleformMap[1].key]}-${obj[addRoleformMap[2].key]}-${obj[addRoleformMap[3].key]}-${
          obj[addRoleformMap[4].key]
        }`,
      };
      if (obj[addRoleformMap[0].key] || obj[addRoleformMap[1].key] || obj[addRoleformMap[2].key] || obj[addRoleformMap[3].key] || obj[addRoleformMap[4].key]) {
        arr.push(realDataStructure);
      }
    });
    return arr;
  };

  const xFormModalConfig = {
    formMap: [
      [
        {
          key: "type",
          label: "集群类型",
          type: FormItemType.select,
          defaultValue: 4,
          options: PHY_CLUSTER_TYPE,
          attrs: {
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
      ],
      [
        {
          key: "project",
          label: "所属项目",
          type: FormItemType.text,
          customFormItem: <RenderText text={props.app.appInfo()?.name} />,
        },
        {
          key: "esVersion",
          label: "集群版本",
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
          },
        },
      ],
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
          placeholder: "请输入集群描述",
        },
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
    width: 850,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: { [key: string]: any }) => {
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
        const roleClusterHosts = getRoleClusterHosts(result);
        contentObj = {
          type: 4,
          tags: JSON.stringify({
            createSource: 1, // 0接入1新建
          }),
          phyClusterName: result.cluster,
          dataCenter: result.dataCenter,
          nsTree: result.nsTree,
          esVersion: result.esVersion,
          desc: result.desc,
          pidCount: result.pidCount,
          roleClusterHosts: roleClusterHosts,
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

  const hostItem = [
    {
      key: "masternode",
      label: "Masternode",
      type: FormItemType.custom,
      customFormItem: <AddRoleTable />,
      rules: [
        {
          required: true,
          validator: (rule: any, value: any) => {
            // console.log($ref.current, 888);
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
    label: "集群版本",
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
    },
  } as IFormItem;

  const esVersionDocker = {
    key: "esVersion",
    label: "集群版本",
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
    },
  } as IFormItem;

  // 默认选中 host，更新选中 host 后的配置项
  // xFormModalConfig.formMap.splice(5, 2, ...hostItem);
  // xFormModalConfig.formMap.splice(4, 1, esVersionHost);
  // 物理集群-新建集群，责任人暂时注释，剪切下标需要减一
  xFormModalConfig.formMap.splice(2, 2, ...hostItem);
  (xFormModalConfig.formMap[1] as any).splice(1, 1, esVersionHost);

  // 这是一段比较恶心的代码 xform的值无法传下去 但是内部需要接收到
  React.useEffect(() => {
    return () => {
      (window as any).masternodeErr = false;
      delete (window as any).masternodeErr;
      delete (window as any).formData;
    };
  }, []);

  const { loading } = props.params;

  const onHandleValuesChange = (value: any, allValues: object) => {
    (window as any).formData = {
      value,
      allValues,
    };
  };

  return (
    <>
      {!loading ? (
        <XFormWrapper onHandleValuesChange={onHandleValuesChange} type={"drawer"} visible={true} {...xFormModalConfig} />
      ) : (
        <span>loading...</span>
      )}
    </>
  );
};

export default connect(mapStateToProps)(ApplyPhyClusterModal);
