import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import * as actions from "actions";
import RegionTransfer from "container/custom-form/region-transfer";
import { getRegionNode, divideRegionCheck, divideRegion, getRegionList, getRackRegionList } from "api/cluster-api";
import { Input, Button, Drawer, Radio, Tag, Tooltip } from "antd";
import { INodeDivide } from "typesPath/index-types";
import { DTable } from "component/dantd/dtable";
import { renderMoreText } from "container/custom-component";
import { regRegionName } from "constants/reg";
import { XNotification } from "component/x-notification";
import { uuid } from "lib/utils";
import { Select } from "antd";
import { DIVIDE_TYPE } from "constants/common";
import "./index.less";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

const RegionDivide = (props: {
  dispatch: any;
  cb: Function;
  params: {
    cluster: string;
    nodeDivideList: INodeDivide[];
    record?: INodeDivide;
    id: number;
    divideType?: any;
  };
}) => {
  const [transferList, setTransferList] = useState([]);
  const [selectKeys, setSelectKeys] = useState([]);
  const [tableList, setTableList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [name, setName] = useState("");
  const [error, setError] = useState(false);
  const [value, setValue] = useState(false);
  const [divideList, setDivideList] = useState([]);
  const [divideType, setDivideType] = useState("host");
  const [flag, setFlag] = useState(true);
  const [attribute, setAttribute] = useState("");

  useEffect(() => {
    _getRegionNode();
  }, []);

  useEffect(() => {
    !flag && changeDivideType();
  }, [attribute]);

  const _getRegionNode = async () => {
    let clusterId = props.params.id;
    setLoading(true);
    await _getRegionList()
      .then(async (res) => {
        let type = "host";
        let rackList = [];
        if (res && res[0]?.divideAttributeKey) {
          type = res[0]?.divideAttributeKey;
          rackList = await _getRackRegionList(type);
          setDivideType("attribute");
          setAttribute(res[0]?.divideAttributeKey);
        }
        let nodes = await getRegionNode(clusterId, type);
        let transferData = getTransferList(nodes, type);
        setTransferList(transferData);
        setTableList(type === "host" ? res : rackList);
        flag && setFlag(false);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  const changeDivideType = async () => {
    let clusterId = props.params.id;
    if (divideType !== "host") {
      let nodes = await getRegionNode(clusterId, attribute);
      let transferData = await getTransferList(nodes, attribute);
      setTransferList(transferData);
    } else {
      _getRegionNode();
    }
  };

  const onChange = (e) => {
    setValue(e.target.value);
  };

  const _getRegionList = async () => {
    let cluster = props.params.cluster;
    let res = await getRegionList(cluster);
    let data = (res || []).map((item) => {
      return {
        ...item,
        regionName: item.name,
        nodeSet: item.nodeNames || "-",
      };
    });
    return data;
  };

  const _getRackRegionList = async (type) => {
    let cluster = props.params.cluster;
    let res = await getRackRegionList(cluster, type);
    let data = (res || []).map((item) => {
      return {
        ...item,
        regionName: item.name,
        nodeSet: item.nodeNames || "-",
      };
    });
    return data;
  };

  const getTransferList = (data, type: string) => {
    let list = data.filter((item) => item.regionId === -1);
    // rack 划分需要过滤掉 attributeValue 为 null 的节点
    if (type !== "host") {
      list = list.filter((item) => item.attributeValue);
    }
    let res = list.map((item) => {
      return {
        ...item,
        key: item.id,
        ids: [item.id],
      };
    });
    // rack 划分方式需要拼接 attributeValue 相同的 ip、nodeSet
    if (type !== "host" && res.length) {
      let rackList = [res[0]];
      let attributeValueList = [res[0].attributeValue];
      (res || []).forEach((item) => {
        for (let i = 0; i < rackList.length; i++) {
          let rack = rackList[i];
          if (item.attributeValue === rack.attributeValue && rack.ip !== item.ip && rack.nodeSet !== item.nodeSet) {
            rack.ip = rack.ip + "，" + item.ip;
            rack.nodeSet = rack.nodeSet + "，" + item.nodeSet;
            let ids = JSON.parse(JSON.stringify(rack?.ids));
            rack.ids = [...ids, item.id];
          } else {
            !attributeValueList.includes(item.attributeValue) && rackList.push(item);
            attributeValueList.push(item.attributeValue);
          }
        }
      });
      return rackList;
    }
    return res;
  };

  const addRegion = async () => {
    if (error) {
      XNotification({ type: "error", message: "请输入正确输入Region名称" });
      return;
    }
    if (!name) {
      XNotification({ type: "error", message: "请输入Region名称" });
      return;
    }
    if (!selectKeys.length) {
      XNotification({ type: "error", message: "请选择加入Region的节点" });
      return;
    }
    let config = { cold: value };
    let params = [] as any;
    let nodes: any = {
      bindingNodeIds: selectKeys,
      logicClusterIds: props.params.id,
      name,
      phyClusterName: props.params.cluster,
      config: JSON.stringify(config),
    };
    // 根据 rack 划分，需要新增 divideAttributeKey 入参
    if (divideType !== "host") {
      nodes.divideAttributeKey = attribute;
      let bindingNodes = nodes.bindingNodeIds;
      transferList.forEach((item) => {
        let ids = item.ids.slice(1);
        if (selectKeys.includes(item.id)) {
          bindingNodes.push(...ids);
        }
      });
      nodes.bindingNodeIds = bindingNodes;
    }
    params.push(nodes);
    if (!value) {
      delete params[0].config;
    }
    // 校验region名称是否重复
    await divideRegionCheck(params);
    setName("");
    setDivideList([...divideList, ...params]);
    // transfer 数据中过滤掉已选中的节点
    let transferData = transferList?.filter((item) => !selectKeys.includes(item?.id));
    setTransferList(transferData);
    // table 展示添加的节点
    let selectList = transferList?.filter((item) => selectKeys.includes(item?.id));
    let nodeSet = selectList.map((item) => item.nodeSet).join(",");
    let attributeValues = selectList.map((item) => item.attributeValue).join(",");
    setTableList([...tableList, { regionName: name, nodeSet, config: JSON.stringify(config), attributeValues, id: uuid() }]);
    setSelectKeys([]);
  };

  const getColumns = () => {
    let columns = [
      {
        title: "Region名称",
        dataIndex: "regionName",
        key: "regionName",
        width: 264,
        render: (val: string, record: any) => {
          let cold = false;
          if (record?.config) {
            let config = JSON.parse(record?.config);
            cold = config?.cold;
          }
          return cold ? (
            <div className="cold-region-tag">
              {renderMoreText(val, 36)}
              <Tooltip title="该Region只用于冷热分离，不可用于划分逻辑集群" overlayStyle={{ maxWidth: 1080 }}>
                <Tag color="blue" className="tag-bule">
                  cold
                </Tag>
              </Tooltip>
            </div>
          ) : (
            renderMoreText(val, 36)
          );
        },
      },
      {
        title: "attribute",
        dataIndex: "attributeValues",
        key: "attributeValues",
      },
      {
        title: "节点名称",
        dataIndex: "nodeSet",
        key: "nodeSet",
      },
    ];
    if (divideType === "host") {
      columns.splice(1, 1);
    }
    return columns;
  };

  const xFormModalConfig = {
    visible: true,
    width: 1080,
    title: "Region划分",
    className: "region-container",
    onClose: () => {
      props.dispatch(actions.setModalId(""));
    },
    footer: (
      <div>
        <Button
          type="primary"
          style={{ marginRight: 10 }}
          onClick={async () => {
            if (divideList.length) {
              await divideRegion(divideList);
              XNotification({ type: "success", message: "添加成功" });
            }
            props.dispatch(actions.setModalId(""));
          }}
        >
          确定
        </Button>
        <Button onClick={() => props.dispatch(actions.setModalId(""))}>取消</Button>
      </div>
    ),
  };

  const renderDivideType = () => {
    let options = (props.params?.divideType || []).map((item) => ({ value: item, label: item }));
    return (
      <div className="divide-type-container">
        <div className="divide-type">
          <div className="title">划分方式：</div>
          <Select
            disabled={tableList.length ? true : false}
            options={DIVIDE_TYPE}
            onChange={(val) => {
              setDivideType(val);
              if (val === "host") {
                setAttribute("");
              } else {
                if (!options.length) {
                  XNotification({ type: "error", message: "该集群节点无可支持划分的attribute信息" });
                  setTransferList([]);
                  setAttribute(undefined);
                } else {
                  setAttribute(options[0]?.value);
                }
              }
            }}
            value={divideType}
          ></Select>
        </div>
        {divideType === "attribute" && (
          <div className="divide-attribute">
            <div className="title">attribute：</div>
            <Select
              placeholder="请选择"
              disabled={tableList.length ? true : false}
              options={options}
              value={attribute ? attribute : undefined}
              onChange={(val: string) => setAttribute(val)}
            ></Select>
          </div>
        )}
      </div>
    );
  };

  const renderDefineRegion = () => {
    return (
      <div>
        <div className="region-title defined">Region定义</div>
        <div className="define-region">
          <div className="region-input">
            <div className="region-name-title">Region名称</div>
            <Input
              allowClear
              value={name}
              className={`region-name-input ${error && "region-name-input-error"}`}
              placeholder="请输入Region名称"
              onChange={(e) => {
                let value = e.target.value;
                if (regRegionName.test(value) && value.length <= 32) {
                  setError(false);
                } else {
                  setError(true);
                }
                setName(value);
              }}
            />
            <Button className="region-name-add" type="primary" onClick={addRegion}>
              添加
            </Button>
            {error && <div className="region-name-error">支持文字，字母，数字，下划线，中划线，32字以内</div>}
          </div>
          <div className="cold-region">
            <div className="cold-region-title">是否定义为cold属性Region？</div>
            <Radio.Group className="cold-group" onChange={onChange} value={value}>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </Radio.Group>
          </div>
        </div>
      </div>
    );
  };

  return (
    <>
      <Drawer {...xFormModalConfig}>
        <div className="warning-container">
          <span className="icon iconfont iconbiaogejieshi"></span>
          <span>Region中节点规格需要保持严格一致，请谨慎操作。</span>
        </div>
        <div className="node-list">
          {renderDivideType()}
          <div className="region-title">节点列表</div>
          <RegionTransfer
            dataSource={transferList}
            selectKeys={(keys) => {
              setSelectKeys(keys);
            }}
            style={{ marginTop: -6 }}
            attribute={attribute}
          />
        </div>
        {renderDefineRegion()}
        <div className="region-title nodelist">Region列表</div>
        <DTable
          loading={loading}
          rowKey="id"
          dataSource={tableList}
          columns={getColumns()}
          attrs={{ size: "small", style: { marginTop: -15 }, pagination: false }}
        />
      </Drawer>
    </>
  );
};

export default connect(mapStateToProps)(RegionDivide);
