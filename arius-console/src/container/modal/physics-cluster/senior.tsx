import React, { useEffect, useState } from "react";
import { Button, Tooltip, Form, Select, Input, Col, Row } from "antd";
import { getDataCenter, getResourceType } from "api/cluster-api";
import { DoubleRightOutlined } from "@ant-design/icons";
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

  const renderCommonForm = () => {
    return (
      <Row gutter={[24, 16]}>
        <Col span={12}>
          <Form.Item name="dataCenter" label="数据中心">
            <Select placeholder="请选择" options={dataCenter}></Select>
          </Form.Item>
        </Col>
        <Col span={12}>
          <Form.Item
            name="usename"
            label={
              <div className="cluster-label">
                账户名
                <Tooltip
                  title={`集群具备账号和密码的用户请自定义添加，不具备请忽略，否则可能导致集群${
                    props?.type === "apply" ? "新建" : "接入"
                  }失败。`}
                >
                  <span className="icon iconfont iconinfo"></span>
                </Tooltip>
              </div>
            }
            rules={[
              {
                validator: async (rule: any, value: string) => {
                  if (value?.length > 32 || value?.includes("：") || value?.includes(":")) {
                    return Promise.reject("请填写账户名，1-32位字符，不支持：号");
                  }
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Input placeholder="请输入账户名"></Input>
          </Form.Item>
        </Col>
      </Row>
    );
  };

  const renderPassword = (
    <Form.Item
      name="password"
      label="密码"
      rules={[
        {
          validator: async (rule: any, value: string) => {
            if (!value) return Promise.resolve();
            if (value.length < 6 || value.length > 32 || /[\u4e00-\u9fa5\:：.]/.test(value)) {
              return Promise.reject("请填写正确密码，6-32位字符，不支持.、:、中文");
            }
            return Promise.resolve();
          },
        },
      ]}
    >
      <Input placeholder="请输入密码"></Input>
    </Form.Item>
  );

  const clusterType = (
    <Form.Item
      initialValue={RESOURCE_TYPE_LIST[1].value}
      name="clusterType"
      label={
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
      }
      rules={[{ required: true }]}
    >
      <Select placeholder="请选择" options={RESOURCE_TYPE_LIST}></Select>
    </Form.Item>
  );

  const proxyAddress = (
    <Form.Item
      name="proxyAddress"
      label={
        <div className="proxy-address">
          代理地址
          <Tooltip title="请填写代理地址，填写后，上方填写的部署地址无效，以代理地址为准">
            <span className="icon iconfont iconinfo"></span>
          </Tooltip>
        </div>
      }
      rules={[
        {
          validator: (_, value) => {
            if (value?.length > 128) return Promise.reject("请输入代理地址，0-128位字符");
            return Promise.resolve();
          },
        },
      ]}
    >
      <Input placeholder="请输入"></Input>
    </Form.Item>
  );

  const kibanaAddress = (
    <Form.Item
      name="kibanaAddress"
      label="kibana外链地址"
      rules={[
        {
          validator: async (rule: any, value: string) => {
            if (!value || (value && value.length <= 512 && /[\u4e00-\u9fa5-_、/.a-zA-Z0-9_]{1,512}$/.test(value))) {
              return Promise.resolve();
            }
            return Promise.reject("清输入正确的kibana外链地址，支持中英文、数字、-、_、、/、.，0-512位字符");
          },
        },
      ]}
    >
      <Input placeholder="请输入kibana外链地址"></Input>
    </Form.Item>
  );

  const cerebroAddress = (
    <Form.Item
      name="cerebroAddress"
      label="cerebro外链地址"
      rules={[
        {
          validator: async (rule: any, value: string) => {
            if (!value || (value && value.length <= 512 && /[\u4e00-\u9fa5-_、/.a-zA-Z0-9_]{1,512}$/.test(value))) {
              return Promise.resolve();
            }
            return Promise.reject("清输入正确的cerebro外链地址，支持中英文、数字、-、_、、/、.，0-512位字符");
          },
        },
      ]}
    >
      <Input placeholder="请输入"></Input>
    </Form.Item>
  );

  const renderAccessForm = () => {
    return (
      <>
        <Row gutter={[24, 16]}>
          <Col span={12}>
            <Form.Item name="platformType" label="IaaS平台类型">
              <Select placeholder="请选择" options={resourceType}></Select>
            </Form.Item>
          </Col>
          <Col span={12}>{renderPassword}</Col>
        </Row>
        <Row gutter={[24, 16]}>
          <Col span={12}>{clusterType}</Col>
          <Col span={12}>{proxyAddress}</Col>
        </Row>
      </>
    );
  };

  const renderApplyForm = () => {
    return (
      <>
        <Row gutter={[24, 16]}>
          <Col span={12}>{clusterType}</Col>
          <Col span={12}>{renderPassword}</Col>
        </Row>
        <Row gutter={[24, 16]}>
          <Col span={24}>{proxyAddress}</Col>
        </Row>
        <Row gutter={[24, 16]}>
          <Col span={12}>{kibanaAddress}</Col>
          <Col span={12}>{cerebroAddress}</Col>
        </Row>
      </>
    );
  };

  const renderContent = () => {
    return (
      <>
        {renderCommonForm()}
        {props?.type === "apply" ? renderApplyForm() : renderAccessForm()}
      </>
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
