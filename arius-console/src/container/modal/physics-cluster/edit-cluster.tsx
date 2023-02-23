import * as React from "react";
import { XFormWrapper } from "component/x-form-wrapper";
import { connect } from "react-redux";
import * as actions from "actions";
import { FormItemType, IFormItem } from "component/x-form";
import { RenderText } from "container/custom-form";
import { VERSION_MAINFEST_TYPE } from "constants/status-map";
import { IOpPhysicsClusterDetail } from "typesPath/cluster/cluster-types";
import { improveOpEditCluster } from "api/cluster-api";
import { XNotification } from "component/x-notification";
import { regIp, regPort } from "constants/reg";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const EditPhyCluster = (props: { dispatch: any; cb: Function; params: IOpPhysicsClusterDetail }) => {
  let { password, proxyAddress, kibanaAddress, cerebroAddress } = props.params;
  const checkText = (value) => {
    const values = value?.split(",").filter((ele: string) => ele !== "") || [];
    const IP_TIP = "请输入IP:端口号，例如：127.1.1.1:8888。多个IP用逗号分割";

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

  const formMap = () => {
    let { password, proxyAddress } = props.params;
    let formItemMap = [
      {
        key: "name",
        label: "集群名称",
        type: FormItemType.text,
        customFormItem: <RenderText text={props.params.cluster} />,
      },
      {
        key: "usename",
        label: "账户名",
        attrs: {
          placeholder: "请输入账户名",
        },
        invisible: !password,
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (value.length > 32 || value.includes("：") || value.includes(":") || !value) {
                return Promise.reject("请填写账户名，1-32位字符，不支持：号");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "password",
        label: "密码",
        invisible: !password,
        attrs: {
          placeholder: "请输入密码",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (value.length < 6 || value.length > 32 || /[\u4e00-\u9fa5\:：.]/.test(value)) {
                return Promise.reject("请填写正确密码，6-32位字符，不支持.、:、中文");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "proxyAddress",
        label: "代理地址",
        type: FormItemType.input,
        invisible: !proxyAddress,
        attrs: {
          placeholder: "请输入代理地址",
        },
        rules: [
          {
            required: true,
            validator: (_, value) => {
              if (value?.length > 128 || !value) return Promise.reject("请输入代理地址，1-128位字符");
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "httpAddress",
        label: "读地址",
        type: FormItemType.input,
        invisible: proxyAddress,
        attrs: {
          placeholder: "请输入",
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (!value) return Promise.reject("请输入读地址");
              let errormsg = checkText(value);
              if (errormsg) {
                return Promise.reject(errormsg);
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "httpWriteAddress",
        label: "写地址",
        type: FormItemType.input,
        invisible: proxyAddress,
        attrs: { placeholder: "请输入" },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (!value) return Promise.reject("请输入写地址");
              let errormsg = checkText(value);
              if (errormsg) {
                return Promise.reject(errormsg);
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "kibanaAddress",
        label: "kibana外链地址",
        attrs: {
          placeholder: "请输入kibana外链地址",
        },
        // invisible: !kibanaAddress,
        isCustomStyle: true,
        CustomStyle: { marginTop: 0 },
        rules: [
          {
            validator: async (rule: any, value: string) => {
              if (!value || (value && value.length <= 512 && /[\u4e00-\u9fa5-_、/.a-zA-Z0-9_]{1,512}$/.test(value))) {
                return Promise.resolve();
              }
              return Promise.reject("清输入正确的kibana外链地址，支持中英文、数字、-、_、、/、.，0-512位字符");
            },
          },
        ],
      },
      {
        key: "cerebroAddress",
        label: "cerebro外链地址",
        // invisible: !cerebroAddress,
        attrs: {
          placeholder: "请输入cerebro外链地址",
        },
        isCustomStyle: true,
        CustomStyle: { marginTop: 0 },
        rules: [
          {
            validator: async (rule: any, value: string) => {
              if (!value || (value && value.length <= 512 && /[\u4e00-\u9fa5-_、/.a-zA-Z0-9_]{1,512}$/.test(value))) {
                return Promise.resolve();
              }
              return Promise.reject("清输入正确的cerebro外链地址，支持中英文、数字、-、_、、/、.，0-512位字符");
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
            validator: (rule: any, value: string) => {
              if (!value || value?.trim().length < 100) {
                return Promise.resolve();
              } else {
                return Promise.reject("请输入0-100字描述信息");
              }
            },
          },
        ],
        attrs: {
          placeholder: "请输入0-100字集群描述",
        },
      },
    ];
    return formItemMap;
  };

  const xFormModalConfig = {
    formMap: formMap() as IFormItem[],
    visible: true,
    title: "编辑集群",
    formData: props.params || {},
    isWaitting: true,
    width: 660,
    needSuccessMessage: false,
    onCancel: () => {
      props.dispatch(actions.setModalId(""));
    },
    onSubmit: (result: any) => {
      let password = result.usename && result.password ? `${result.usename}:${result.password}` : undefined;
      const req = props.params;
      const { desc, cerebroAddress, httpAddress, httpWriteAddress, kibanaAddress, proxyAddress } = result;
      req.desc = desc;
      let params = {
        id: props.params.id,
        desc,
        cerebroAddress,
        password,
        proxyAddress,
        httpAddress,
        httpWriteAddress,
        kibanaAddress,
      };
      return improveOpEditCluster(params)
        .then(() => {
          XNotification({ type: "success", message: "编辑成功" });
        })
        .finally(() => {
          props.cb && props.cb();
          props.dispatch(actions.setModalId(""));
        });
    },
  };

  return (
    <>
      <XFormWrapper visible={true} {...xFormModalConfig} />
    </>
  );
};

export default connect(mapStateToProps)(EditPhyCluster);
