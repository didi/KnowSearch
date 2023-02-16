import React, { useState } from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { regClusterName } from "constants/reg";
import { INode } from "typesPath/cluster/cluster-types";
import { AppState, UserState } from "store/type";
import { creatCluster } from "api/cluster-api";
import { Masternode } from "container/custom-form";
import { AddRoleTable, nodeTypeList } from "container/custom-form/add-role-table";
import { addRoleformMap } from "container/custom-form/tpl-table-add-row/editTable";
import { RESOURCE_TYPE_LIST } from "constants/common";
import Senior from "./senior";
import { showSubmitTaskSuccessModal } from "container/custom-component";
import { Tooltip } from "antd";

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
  const [seniorValue, setSeniorValue] = useState({} as any);

  const getRoleClusterHosts = (result) => {
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
        machineSpec: obj.machineSpec,
      };
      if (
        obj[addRoleformMap[0].key] ||
        obj[addRoleformMap[1].key] ||
        obj[addRoleformMap[2].key] ||
        obj[addRoleformMap[3].key] ||
        obj[addRoleformMap[4].key]
      ) {
        arr.push(realDataStructure);
      }
    });
    return arr;
  };

  const xFormModalConfig = {
    formMap: [
      [
        {
          key: "phyClusterName",
          label: "集群名称",
          attrs: {
            placeholder: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
          },
          isCustomStyle: true,
          CustomStyle: { marginBottom: 0 },
          rules: [
            {
              required: true,
              message: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
              validator: async (rule: any, value: string) => {
                const reg = /^[a-zA-Z0-9_-]{1,}$/g;
                if (!reg.test(value) || value?.length > 32 || !value) {
                  return Promise.reject("请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符");
                }
                return Promise.resolve();
              },
            },
          ],
        },
        {
          key: "esVersion",
          label: "集群版本",
          type: FormItemType.select,
          options: props.params?.packageHostList || [],
          isCustomStyle: true,
          CustomStyle: { marginBottom: 0 },
          rules: [
            {
              required: true,
              message: "请选择集群版本",
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
      // {
      //   key: "masternode",
      //   label: "Masternode",
      //   type: FormItemType.custom,
      //   customFormItem: <Masternode nodeList={props.params?.nodeList || []} />,
      //   rules: [
      //     {
      //       required: true,
      //       whitespace: true,
      //       validator: async (rule: any, value) => {
      //         if (value) {
      //           return Promise.resolve();
      //         }
      //         return Promise.reject();
      //       },
      //     },
      //   ],
      // },
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
        key: "senior",
        className: "apply-senior",
        type: FormItemType.custom,
        customFormItem: <Senior seniorChange={(val) => setSeniorValue(val)} type="apply" seniorValue={seniorValue} />,
      },
    ] as IFormItem[],
    visible: true,
    title: "新建集群",
    formData: { resourceType: RESOURCE_TYPE_LIST[1].value },
    width: 850,
    needBtnLoading: true,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: async (result: { [key: string]: any }) => {
      let clusterRoleHosts = getRoleClusterHosts(result);
      let password = seniorValue.usename && seniorValue.password ? `${seniorValue.usename}:${seniorValue.password}` : "";
      let params = {
        type: 4,
        clusterRoleHosts,
        esVersion: result.esVersion,
        phyClusterName: result.phyClusterName,
        desc: result.desc,
        tags: JSON.stringify({
          createSource: 1, // 0接入1新建
        }),
        dataCenter: seniorValue.dataCenter || "",
        resourceType: Number(seniorValue.clusterType) || 2, // 集群类型默认为独立集群
        proxyAddress: seniorValue.proxyAddress,
        kibanaAddress: seniorValue.kibanaAddress,
        cerebroAddress: seniorValue.cerebroAddress,
        password,
      };
      let expandData = JSON.stringify(params);
      let ret = await creatCluster({ expandData });
      props.dispatch(actions.setModalId(""));
      showSubmitTaskSuccessModal(ret, props.params?.history);
    },
  };

  const hostItem = [
    {
      key: "masternode",
      label: "Masternode",
      type: FormItemType.custom,
      customFormItem: <AddRoleTable machineList={props.params?.machineList || []} />,
      rules: [
        {
          required: true,
          validator: (rule: any, value: any) => {
            return Promise.resolve();
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

  // 默认选中 host，更新选中 host 后的配置项
  // xFormModalConfig.formMap.splice(5, 2, ...hostItem);
  // xFormModalConfig.formMap.splice(4, 1, esVersionHost);
  // 物理集群-新建集群，责任人暂时注释，剪切下标需要减一
  xFormModalConfig.formMap.splice(1, 1, ...hostItem);
  // (xFormModalConfig.formMap[1] as any).splice(1, 1, esVersionHost);

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
