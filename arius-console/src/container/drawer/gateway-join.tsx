import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import { Dispatch } from "redux";
import { FormItemType, IFormItem } from "component/x-form";
import { XFormWrapper } from "component/x-form-wrapper";
import { getDataCenter } from "api/cluster-api";
import { joinGateway } from "api/gateway-manage";
import { regIp, regPort } from "constants/reg";
import { Tooltip, Button } from "antd";
import { DoubleRightOutlined } from "@ant-design/icons";
import "./index.less";

const mapDispatchToProps = (dispatch: Dispatch) => ({
  setModalId: (modalId: string, params?: any, cb?: Function) => dispatch(actions.setModalId(modalId, params, cb)),
  setDrawerId: (drawerId: string, params?: any, cb?: Function) => dispatch(actions.setDrawerId(drawerId, params, cb)),
});

const mapStateToProps = (state) => ({
  app: state.app,
  cb: state.modal.cb,
});

const JoinGateway = ({ app, cb, setModalId, setDrawerId }) => {
  const [fold, setFold] = useState(false);
  const [dataCenter, setDataCenter] = useState([]);

  const _getDataCenter = async () => {
    let res = await getDataCenter();
    let data = (res || []).map((item: string) => {
      return { value: item, label: item };
    });
    setDataCenter(data);
  };

  useEffect(() => {
    _getDataCenter();
  }, []);

  const xFormModalConfig = () => {
    const gatewayClusterNameTips = "请填写Gateway集群名称，支持大、小写字母、数字、-、_，1-32位字符";
    const gatewayHostsTips = "支持填写IP:端口号、hostname:端口号或域名";
    let formMap = [
      {
        key: "gatewayClusterName",
        label: "Gateway集群名称",
        type: FormItemType.input,
        attrs: {
          placeholder: gatewayClusterNameTips,
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              const reg = /^[a-zA-Z0-9_-]{1,}$/g;
              if (!reg.test(value) || value?.length > 32 || !value) {
                return Promise.reject(gatewayClusterNameTips);
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "gatewayHosts",
        label: "实例名称",
        type: FormItemType.textArea,
        attrs: {
          placeholder: gatewayHostsTips,
        },
        rules: [
          {
            required: true,
            validator: async (rule: any, value: string) => {
              if (!value) {
                return Promise.reject(gatewayHostsTips);
              }
              const arr = value.split(":");
              if (!new RegExp(regIp).test(arr[0])) {
                return Promise.reject("请填写正确的IP：端口号、hostname：端口号或域名，如127.1.1.1:8888");
              }
              if (!new RegExp(regPort).test(arr[1])) {
                return Promise.reject("请填写正确的IP：端口号、hostname：端口号或域名，如127.1.1.1:8888");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "memo",
        label: "描述",
        type: FormItemType.textArea,
        className: "gateway-join-memo",
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
          placeholder: "请输入Gateway集群描述",
          rows: 8,
        },
      },
      {
        key: "senior",
        className: "access-senior",
        type: FormItemType.custom,
        customFormItem: (
          <div className="access-cluster-senior">
            <Button type="link" style={{ paddingLeft: 0 }} onClick={() => setFold(!fold)}>
              高级
              <DoubleRightOutlined className={fold ? "up" : "down"} />
            </Button>
          </div>
        ),
      },
      {
        key: "dataCenter",
        label: "数据中心",
        type: FormItemType.select,
        invisible: !fold,
        options: dataCenter,
        attrs: {
          placeholder: "请选择",
        },
        rules: [{ required: false, message: "请选择数据中心" }],
      },
      {
        key: "proxyAddress",
        label: (
          <div className="tip-label">
            <span>代理地址</span>

            <Tooltip title="请填写代理地址，填写后，上方填写的部署地址无效，以代理地址为准">
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.input,
        invisible: !fold,
        attrs: {
          placeholder: "请输入",
        },
        rules: [
          {
            required: false,
            message: "请输入代理地址，0-128位字符",
            validator: async (rule: any, value: string) => {
              if (!value || value?.trim().length <= 128) {
                return Promise.resolve();
              } else {
                return Promise.reject();
              }
            },
          },
        ],
      },
    ] as IFormItem[];
    return {
      formMap,
      type: "drawer",
      title: "接入Gateway",
      width: 480,
      visible: true,
      needBtnLoading: true,
      onCancel: () => setDrawerId(""),
      onSubmit: async (result) => {
        const params = {
          clusterName: result.gatewayClusterName,
          dataCenter: result.dataCenter,
          gatewayNodeHosts: [
            {
              hostName: result.gatewayHosts.split(":")[0],
              port: +result.gatewayHosts.split(":")[1],
            },
          ],
          memo: result.memo,
          proxyAddress: result.proxyAddress,
        };
        await joinGateway(params);
        setDrawerId("");
        cb();
      },
    };
  };

  return <XFormWrapper {...xFormModalConfig()} />;
};

export default connect(mapStateToProps, mapDispatchToProps)(JoinGateway);
