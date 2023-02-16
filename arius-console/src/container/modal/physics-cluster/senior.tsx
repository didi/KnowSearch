import React, { useEffect, useState } from "react";
import { Button, Tooltip } from "antd";
import { getDataCenter, getResourceType } from "api/cluster-api";
import { DoubleRightOutlined } from "@ant-design/icons";
import { FormItemType, IFormItem, XForm as XFormComponent } from "component/x-form";
import { RESOURCE_TYPE_LIST } from "constants/common";
import "./index.less";

export default function Senior(props) {
  const [fold, setFold] = useState(false);
  const [dataCenter, setDataCenter] = useState([]);
  const [resourceType, setResourceType] = useState([]);

  useEffect(() => {
    _getDataCenter();
    _getResourceType();
  }, []);

  const _getDataCenter = async () => {
    let res = await getDataCenter();
    let data = (res || []).map((item: string) => {
      return { value: item, label: item };
    });
    setDataCenter(data);
  };

  const _getResourceType = async () => {
    let res = await getResourceType();
    let data = (res || []).map((item: string) => {
      return { value: item, label: item };
    });
    setResourceType(data);
  };

  const onHandleValuesChange = (val, allVal) => {
    props.seniorChange(allVal);
  };

  const accessFormMap = [
    props?.type === "apply"
      ? {
          key: "proxyAddress",
          label: (
            <div className="cluster-label">
              代理地址
              <Tooltip title="请填写代理地址，填写后，上方填写的部署地址无效，以代理地址为准。">
                <span className="icon iconfont iconinfo"></span>
              </Tooltip>
            </div>
          ),
          isCustomStyle: true,
          CustomStyle: { marginTop: 0 },
          attrs: {
            placeholder: "请输入",
          },
          rules: [
            {
              validator: (_, value) => {
                if (value?.length > 128) return Promise.reject("请输入代理地址，0-128位字符");
                return Promise.resolve();
              },
            },
          ],
        }
      : [
          {
            key: "platformType",
            label: "IaaS平台类型",
            type: FormItemType.select,
            options: resourceType,
            attrs: {
              placeholder: "请选择",
            },
            isCustomStyle: true,
            CustomStyle: { marginTop: 0 },
          },
          {
            key: "proxyAddress",
            label: (
              <div className="cluster-label">
                代理地址
                <Tooltip title="请填写代理地址，填写后，上方填写的部署地址无效，以代理地址为准。">
                  <span className="icon iconfont iconinfo"></span>
                </Tooltip>
              </div>
            ),
            isCustomStyle: true,
            CustomStyle: { marginTop: 0 },
            attrs: {
              placeholder: "请输入",
            },
            rules: [
              {
                validator: (_, value) => {
                  if (value?.length > 128) return Promise.reject("请输入代理地址，0-128位字符");
                  return Promise.resolve();
                },
              },
            ],
          },
        ],
    [
      {
        key: "usename",
        label: (
          <div className="cluster-label">
            账户名
            <Tooltip title="集群具备账号和密码的用户请自定义添加，不具备请忽略，否则可能导致集群接入失败。">
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        attrs: {
          placeholder: "请输入账户名",
        },
        isCustomStyle: true,
        CustomStyle: { marginTop: 0 },
        rules: [
          {
            validator: async (_rule: any, value: string) => {
              if (value.length > 32 || value.includes("：") || value.includes(":")) {
                return Promise.reject("请填写账户名，1-32位字符，不支持：号");
              }
              return Promise.resolve();
            },
          },
        ],
      },
      {
        key: "password",
        label: <div className="cluster-label">密码</div>,
        attrs: {
          placeholder: "请输入密码",
        },
        isCustomStyle: true,
        CustomStyle: { marginTop: 0 },
        rules: [
          {
            validator: async (rule: any, value: string) => {
              if (!value) return Promise.resolve();
              if (
                value.length < 6 ||
                value.length > 32 ||
                value.includes(".") ||
                /[\u4e00-\u9fa5]/.test(value) ||
                value.includes(":") ||
                value.includes("：")
              ) {
                return Promise.reject("请填写正确密码，6-32位字符，不支持.、:、中文");
              }
              return Promise.resolve();
            },
          },
        ],
      },
    ],
    [
      {
        key: "kibanaAddress",
        label: "kibana外链地址",
        attrs: {
          placeholder: "请输入kibana外链地址",
        },
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
    ],
  ] as unknown as IFormItem[];

  const basicFormMap = [
    [
      {
        key: "clusterType",
        label: (
          <div className="cluster-label">
            <span className="title">集群类型</span>
            <Tooltip
              title={
                <>
                  <div>独立集群：支持集群层面的数据隔离</div>
                  <div>独享集群：支持数据节点层面的隔离</div>
                  <div>共享集群：数据共享</div>
                </>
              }
            >
              <span className="icon iconfont iconinfo"></span>
            </Tooltip>
          </div>
        ),
        type: FormItemType.select,
        options: RESOURCE_TYPE_LIST,
        rules: [
          {
            required: true,
          },
        ],
        attrs: {
          placeholder: "请选择",
        },
      },
      {
        key: "dataCenter",
        label: "数据中心",
        type: FormItemType.select,
        options: dataCenter,
        attrs: {
          placeholder: "请选择",
        },
      },
    ],
  ];

  const renderContent = () => {
    let formMap = [].concat(basicFormMap).concat(accessFormMap);

    let seniorValue = props?.seniorValue || {};
    return (
      <XFormComponent
        className="senior-form"
        //@ts-ignore
        formMap={formMap}
        formData={{ clusterType: RESOURCE_TYPE_LIST[1].value, ...seniorValue }}
        onHandleValuesChange={onHandleValuesChange}
      ></XFormComponent>
    );
  };

  return (
    <div className="access-cluster-senior">
      <Button type="link" style={{ paddingLeft: 0, marginBottom: "16px" }} onClick={() => setFold(!fold)}>
        高级
        <DoubleRightOutlined className={fold ? "up" : "down"} />
      </Button>
      {fold && renderContent()}
    </div>
  );
}
