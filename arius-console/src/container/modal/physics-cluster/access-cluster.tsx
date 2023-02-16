import React, { useState } from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem } from "component/x-form";
import { XFormWrapper } from "component/x-form-wrapper";
import "./index.less";
import { clusterJoin } from "api/cluster-api";
import { INPUT_RULE_MAP } from "constants/status-map";
import Senior from "./senior";
import { regIp, regPort } from "constants/reg";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
  cb: state.modal.cb,
});

const AccessCluster = ({ app, cb, setModalId }) => {
  const [seniorValue, setSeniorValue] = useState({} as any);
  const [isFullType, setIsFullType] = useState(false);

  const getRoleClusterHosts = async (baseInfoData) => {
    let arrRoleClusterHosts = [];
    let nodeList = [
      { fullnodes: baseInfoData?.fullnodes },
      { masternode: baseInfoData?.masternode },
      { clientnode: baseInfoData?.clientnode },
      { datanode: baseInfoData?.datanode },
    ];
    nodeList.forEach((item) => {
      let key = Object.keys(item)[0];
      const arr = item[key]?.split("\n")?.filter((ele: string) => ele !== "");
      (arr || []).forEach((item) => {
        const ipPostArr = item.split(":");
        const arrRoleClusterHost: any = {
          cluster: baseInfoData.name,
          id: 0,
          ip: ipPostArr[0],
          nodeSet: "",
          port: ipPostArr[1],
          role: key === "datanode" ? 1 : key === "clientnode" ? 2 : 3, //角色 1data   2client    3master
          roleClusterId: 0,
          status: 0,
        };
        arrRoleClusterHosts.push(arrRoleClusterHost);
      });
    });
    return arrRoleClusterHosts;
  };

  const checkText = (value, type) => {
    // 非master不做空判断
    if (!value && type == "masternode") {
      return "请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。";
    }
    const values = value?.split("\n").filter((ele: string) => ele !== "") || [];
    const IP_TIP = "请输入IP:端口号，例如：127.1.1.1:8888。多个IP用换行分割";

    let flat = false;
    let flatPost = false;
    let flatLength = false;
    let flatIp = false;
    const ipArr = [];

    values.forEach((element) => {
      if (element.indexOf(":") === -1) {
        flat = true;
      } else {
        const isPostArr = element.split(":");
        ipArr.push(isPostArr[0]);
        if (!new RegExp(regIp).test(isPostArr[0])) {
          flatIp = true;
        }
        if ((!isPostArr[1] && flatIp === false) || !new RegExp(regPort).test(isPostArr[1])) {
          flatPost = true;
        }
        if (isPostArr[1] && flatIp === false) {
          if (+isPostArr[1] > 25535) {
            flatLength = true;
          }
        }
      }
    });
    // 多个相同 ip
    if (new Set(ipArr).size != ipArr.length) {
      return `${IP_TIP}且 ip 不能相同`;
    } else if (flat || flatIp || flatPost) {
      // 格式错误，ip不符合规范, 没有:号, :号后面没有端口
      return IP_TIP;
    } else if (flatLength) {
      // 格式错误，:号端口格式不正确, 多个请换行。
      return `${IP_TIP}，输入端口号超最大值25535`;
    }
  };

  const xFormModalConfig = () => {
    let formMap = [
      {
        key: "name",
        label: "集群名称",
        attrs: {
          placeholder: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
          style: { width: 432 },
        },
        rules: [
          {
            required: true,
            message: "请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符",
            validator: async (rule: any, value: string) => {
              const reg = /^[a-zA-Z0-9_-]{1,}$/g;
              if (!reg.test(value) || value.length > 32 || !value) {
                return Promise.reject("请填写集群名称，支持大、小写字母、数字、-、_，1-32位字符");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "importRule",
        label: "录入规则",
        type: FormItemType.radioGroup,
        defaultValue: "0",
        className: "import-rule",
        attrs: {
          onChange: (e) => {
            if (e && e.target) {
              let bool = e.target.value === "1";
              setIsFullType(bool);
            }
          },
        },
        options: Object.keys(INPUT_RULE_MAP).map((item) => {
          return {
            label: INPUT_RULE_MAP[item],
            value: item,
          };
        }),
        rules: [{ required: true, message: "请选择录入规则" }],
      },
      {
        key: "fullnodes",
        label: "集群节点",
        type: FormItemType.textArea,
        className: "access-add-role",
        attrs: {
          placeholder:
            "请添加可接入节点，masternode、clientnode、datanode均可，需按照IP：端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: { type: string; value: string; check?: boolean }[]) => {
              let errormsg = checkText(value, "masternode");
              if (errormsg) {
                return Promise.reject(errormsg);
              }
              return Promise.resolve("");
            },
          },
        ],
      },
      {
        key: "senior",
        className: "access-senior",
        type: FormItemType.custom,
        customFormItem: <Senior seniorChange={(val) => setSeniorValue(val)} seniorValue={seniorValue} />,
      },
    ] as IFormItem[];
    if (isFullType) {
      let fullnode = [
        {
          key: "masternode",
          label: "Masternode",
          type: FormItemType.textArea,
          className: "access-add-role",
          attrs: {
            placeholder: "请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。",
          },
          rules: [
            {
              required: true,
              validator: async (rule: any, value: { type: string; value: string; check?: boolean }[]) => {
                let errormsg = checkText(value, "masternode");
                if (errormsg) {
                  return Promise.reject(errormsg);
                }
                return Promise.resolve("");
              },
            },
          ],
        },
        {
          key: "clientnode",
          label: "Clientnode",
          type: FormItemType.textArea,
          className: "access-add-role",
          attrs: {
            placeholder: "请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。",
          },
          rules: [
            {
              validator: async (rule: any, value: { type: string; value: string; check?: boolean }[]) => {
                let errormsg = checkText(value, "clientnode");
                if (errormsg) {
                  return Promise.reject(errormsg);
                }
                return Promise.resolve("");
              },
            },
          ],
        },
        {
          key: "datanode",
          label: "Datanode",
          type: FormItemType.textArea,
          className: "access-add-role",
          attrs: {
            placeholder: "请按照 IP:端口号的形式填写，例如：127.1.1.1:8888 ，不同IP用换行符分隔。",
          },
          rules: [
            {
              validator: async (rule: any, value: { type: string; value: string; check?: boolean }[]) => {
                let errormsg = checkText(value, "datanode");
                if (errormsg) {
                  return Promise.reject(errormsg);
                }
                return Promise.resolve("");
              },
            },
          ],
        },
      ];
      formMap.splice(2, 1, ...fullnode);
    }
    return {
      formMap,
      title: "接入集群",
      width: 480,
      visible: true,
      formData: {},
      needBtnLoading: true,
      onCancel: () => setModalId(""),
      onSubmit: async (result) => {
        const roleClusterHosts = await getRoleClusterHosts(result);
        let password = seniorValue.usename && seniorValue.password ? `${seniorValue.usename}:${seniorValue.password}` : "";
        const params = {
          type: 4,
          resourceType: Number(seniorValue.clusterType) || 2, // 集群类型默认为独立集群
          dataCenter: seniorValue.dataCenter || "",
          platformType: seniorValue.platformType || "",
          tags: JSON.stringify({
            createSource: 0, // 0接入1新建
          }),
          proxyAddress: seniorValue.proxyAddress,
          kibanaAddress: seniorValue.kibanaAddress,
          cerebroAddress: seniorValue.cerebroAddress,
          importRule: result.importRule,
          projectId: app.appInfo()?.id,
          cluster: result.name,
          divide: true,
          logicCluster: result?.logicCluster,
          phyClusterDesc: result.desc || "",
          singular: result.singular,
          roleClusterHosts,
          password,
        };
        await clusterJoin(params);
        setModalId("");
        cb();
      },
    };
  };

  return <XFormWrapper type={"drawer"} {...xFormModalConfig()} />;
};

export default connect(mapStateToProps, mapDispatchToProps)(AccessCluster);
